package io.onedev.server.ee.terminal;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.event.Listen;
import io.onedev.server.event.project.build.BuildEvent;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.job.JobManager;
import io.onedev.server.model.Build;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.terminal.MessageTypes;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.terminal.TerminalManager;
import io.onedev.server.terminal.WebShell;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.apache.wicket.request.cycle.RequestCycle;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultTerminalManager implements TerminalManager, SchedulableTask, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTerminalManager.class);
	
	private final JobManager jobManager;
	
	final ClusterManager clusterManager;
	
	private final TaskScheduler taskScheduler;
	
	private final SessionManager sessionManager;
	
	private final SubscriptionManager subscriptionManager;
	
	private final Map<IWebSocketConnection, WebShell> shells = new ConcurrentHashMap<>();
	
	private volatile String taskId;
	
	@Inject
	public DefaultTerminalManager(JobManager jobManager, TaskScheduler taskScheduler, ClusterManager clusterManager,
								  SessionManager sessionManager, SubscriptionManager subscriptionManager) {
		this.jobManager = jobManager;
		this.taskScheduler = taskScheduler;
		this.clusterManager = clusterManager;
		this.sessionManager = sessionManager;
		this.subscriptionManager = subscriptionManager;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(TerminalManager.class);
	}
	
	@Override
	public void onOpen(IWebSocketConnection connection, Build build) {
		if (subscriptionManager.isSubscriptionActive()) {
			try {
				Long buildId = build.getId();
				String sessionId = UUID.randomUUID().toString();
				Terminal terminal = new ServerTerminal(sessionId, clusterManager.getLocalServerAddress());
				shells.put(connection, jobManager.openShell(buildId, terminal));
			} catch (Throwable t) {
				ExplicitException explicitException = ExceptionUtils.find(t, ExplicitException.class);
				if (explicitException != null) {
					sendError(connection, explicitException.getMessage());
				} else {
					logger.error("Error opening shell", t);
					sendError(connection, "Error opening shell, check server log for details");
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void onClose(IWebSocketConnection connection) {
		var shell = shells.remove(connection);
		if (shell != null) 
			shell.exit();
	}
	
	@Override
	public void onInput(IWebSocketConnection connection, String input) {
		var shell = shells.get(connection);
		if (shell != null) 
			shell.sendInput(input);
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}
	
	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
	}

	@Sessional
	@Listen
	public void on(BuildEvent event) {
		if (event.getBuild().isFinished()) {
			clusterManager.submitToAllServers(new ClusterTask<Void>() {

				private static final long serialVersionUID = 1L;

				@Override
				public Void call() {
					sessionManager.run(new Runnable() {

						@Override
						public void run() {
							for (var it = shells.entrySet().iterator(); it.hasNext();) {
								var entry = it.next();
								WebShell shell = entry.getValue();
								if (shell.getBuildId().equals(event.getBuild().getId())) {
									sendError(entry.getKey(), "Shell exited");
									shell.exit();
									it.remove();
								}
							}
						}
						
					});
					return null;
				}
				
			});
		}
	}

	@Override
	public void execute() {
		for (Iterator<Map.Entry<IWebSocketConnection, WebShell>> it = shells.entrySet().iterator(); it.hasNext();) {
			Map.Entry<IWebSocketConnection, WebShell> entry = it.next();
			WebShell shell = entry.getValue();
			if (!entry.getKey().isOpen()) {
				shell.exit();
				it.remove();
			}
		}
	}
	
	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return SimpleScheduleBuilder.repeatMinutelyForever();
	}

	@Override
	public void onResize(IWebSocketConnection connection, int rows, int cols) {
		WebShell shell = shells.get(connection);
		if (shell != null) 
			shell.resize(rows, cols);
	}

	@Override
	public String getTerminalUrl(Build build) {
		return RequestCycle.get().urlFor(BuildTerminalPage.class, BuildTerminalPage.paramsOf(build)).toString();
	}

	private void sendOutput(IWebSocketConnection connection, String output) {
		try {
			connection.sendMessage(MessageTypes.TERMINAL_OUTPUT + ":" + output);
		} catch (Exception e) {
		}
	}
	
	private void sendError(IWebSocketConnection connection, String error) {
		try {
			connection.sendMessage(MessageTypes.TERMINAL_OUTPUT + ":\r\n\033[31m" + error + "\033[0m");
		} catch (Exception e) {
		}
	}

	private void close(IWebSocketConnection connection) {
		try {
			connection.sendMessage(MessageTypes.TERMINAL_CLOSE.name());
		} catch (Exception e) {
		}
	}

	private class ServerTerminal implements Terminal, Serializable {

		private static final long serialVersionUID = 1L;

		private final String sessionId;
		
		private final String terminalServer;
		
		public ServerTerminal(String sessionId, String terminalServer) {
			this.sessionId = sessionId;
			this.terminalServer = terminalServer;
		}
		
		@Nullable
		private IWebSocketConnection getConnection() {
			for (var entry: shells.entrySet()) {
				if (entry.getValue().getSessionId().equals(sessionId))
					return entry.getKey();
			}
			return null;
		}
		
		@Override
		public void sendOutput(String output) {
			clusterManager.submitToServer(terminalServer, () -> {
				IWebSocketConnection connection = getConnection();
				if (connection != null)
					DefaultTerminalManager.this.sendOutput(connection, output);
				return null;
			});
		}

		@Override
		public void sendError(String error) {
			clusterManager.submitToServer(terminalServer, () -> {
				IWebSocketConnection connection = getConnection();
				if (connection != null)
					DefaultTerminalManager.this.sendError(connection, error);
				return null;
			});
		}

		@Override
		public void close() {
			clusterManager.submitToServer(terminalServer, () -> {
				IWebSocketConnection connection = getConnection();
				if (connection != null)
					DefaultTerminalManager.this.close(connection);
				return null;
			});
		}

		@Override
		public String getSessionId() {
			return sessionId;
		}

	}

}
