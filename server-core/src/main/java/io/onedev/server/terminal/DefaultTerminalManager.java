package io.onedev.server.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.loader.Listen;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.ExposeOutputStream;
import io.onedev.commons.utils.command.PtyMode;
import io.onedev.commons.utils.command.PumpInputToOutput;
import io.onedev.server.buildspec.job.JobManager;
import io.onedev.server.event.build.BuildEvent;
import io.onedev.server.model.Build;
import io.onedev.server.util.schedule.SchedulableTask;

@Singleton
public class DefaultTerminalManager implements TerminalManager, SchedulableTask {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTerminalManager.class);
	
	private final JobManager jobManager;
	
	private final ExecutorService executorService;
	
	private final Map<IWebSocketConnection, TerminalSession> sessions = new ConcurrentHashMap<>();
	
	@Inject
	public DefaultTerminalManager(JobManager jobManager, ExecutorService executorService) {
		this.jobManager = jobManager;
		this.executorService = executorService;
	}
	
	@Override
	public void onOpen(IWebSocketConnection connection, Build build) {
		try {
			Commandline shell = jobManager.openShell(build);
			PtyMode ptyMode = new PtyMode();
			shell.ptyMode(ptyMode);
			
			OutputStream output = new OutputStream() {
	
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					connection.sendMessage(TerminalMessages.OUTPUT + ":" 
							+ new String(b, off, len, StandardCharsets.UTF_8));
				}
	
				@Override
				public void write(int b) throws IOException {
					throw new UnsupportedOperationException();
				}
	
			};
			
			OutputStream error = new OutputStream() {
	
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					sendError(connection, new String(b, off, len, StandardCharsets.UTF_8));
				}
	
				@Override
				public void write(int b) throws IOException {
					throw new UnsupportedOperationException();
				}
	
			};
			
			ExposeOutputStream input = new ExposeOutputStream();
			Future<?> future = executorService.submit(new Runnable() {
	
				@Override
				public void run() {
					try { 
						shell.execute(new PumpInputToOutput(output), new PumpInputToOutput(error), input);
					} finally {
						try {
							connection.sendMessage(TerminalMessages.CLOSE);
						} catch (IOException e) {
						}
						sessions.remove(connection);
					}
				}
				
			});
			sessions.put(connection, new TerminalSession(future, ptyMode, input, build.getId()));
		} catch (ExplicitException e) {
			sendError(connection, e.getMessage());
		} catch (Exception e) {
			logger.error("Error openning shell", e);
			sendError(connection, "Error opening shell, check server log for details");
		}
	}

	@Override
	public void onClose(IWebSocketConnection connection) {
		TerminalSession session = sessions.remove(connection);
		if (session != null) 
			session.terminate();
	}
	
	private void sendError(IWebSocketConnection connection, String error) {
		try {
			connection.sendMessage(TerminalMessages.OUTPUT + ":\033[2J\033[H\033[31m" + error + "\033[0m");
		} catch (IOException e) {
		}
	}

	@Override
	public void onInput(IWebSocketConnection connection, String input) {
		TerminalSession session = sessions.get(connection);
		if (session != null) {
			try {
				session.getShellInput().getOutput().write(input.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				logger.error("Error writing shell input", e);
				sendError(connection,  "Unable to write shell input, check server log for details");
			}
		} else {
			sendError(connection, "Shell exited");
		}
	}

	@Listen
	public void on(BuildEvent event) {
		if (event.getBuild().isFinished()) {
			for (Iterator<Map.Entry<IWebSocketConnection, TerminalSession>> it = sessions.entrySet().iterator(); it.hasNext();) {
				Map.Entry<IWebSocketConnection, TerminalSession> entry = it.next();
				TerminalSession session = entry.getValue();
				if (session.getBuildId().equals(event.getBuild().getId())) {
					session.terminate();
					sendError(entry.getKey(), "Build finished");
					it.remove();
				}
			}
		}
	}

	@Override
	public void execute() {
		for (Iterator<Map.Entry<IWebSocketConnection, TerminalSession>> it = sessions.entrySet().iterator(); it.hasNext();) {
			Map.Entry<IWebSocketConnection, TerminalSession> entry = it.next();
			if (!entry.getKey().isOpen()) {
				entry.getValue().terminate();
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
		TerminalSession session = sessions.get(connection);
		if (session != null) 
			session.getPtyMode().getResizeSupport().resize(rows, cols);
	}
	
}
