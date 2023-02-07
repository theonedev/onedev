package io.onedev.server.plugin.executor.remotedocker;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.DockerJobData;
import io.onedev.agent.job.TestDockerJobData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.Service;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.AgentManager;
import io.onedev.server.job.*;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.LogTask;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.terminal.AgentShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Numeric;
import org.eclipse.jetty.websocket.api.Session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Editable(order=210, description="This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>")
public class RemoteDockerExecutor extends ServerDockerExecutor {

	private static final long serialVersionUID = 1L;
	
	private String agentQuery;

	private transient volatile Session agentSession;
	
	@Editable(order=390, name="Agent Selector", placeholder="Any agent", 
			description="Specify agents applicable for this executor")
	@io.onedev.server.web.editable.annotation.AgentQuery(forExecutor=true)
	public String getAgentQuery() {
		return agentQuery;
	}

	public void setAgentQuery(String agentQuery) {
		this.agentQuery = agentQuery;
	}

	@Editable(order=450, placeholder = "Number of agent cpu", description = "" +
			"Specify max number of jobs/services this executor can run concurrently on each matched agent")
	@Numeric
	@Override
	public String getConcurrency() {
		return super.getConcurrency();
	}

	@Override
	public void setConcurrency(String concurrency) {
		super.setConcurrency(concurrency);
	}
	
	private AgentManager getAgentManager() {
		return OneDev.getInstance(AgentManager.class);
	}
	
	private JobManager getJobManager() {
		return OneDev.getInstance(JobManager.class);
	}

	private SessionManager getSessionManager() {
		return OneDev.getInstance(SessionManager.class);
	}
	
	private int getConcurrencyNumber() {
		if (getConcurrency() != null)
			return Integer.parseInt(getConcurrency());
		else
			return 0;		
	}
	
	@Override
	public void execute(JobContext jobContext, TaskLogger jobLogger) {
		AgentRunnable runnable = (agentId) -> {
			getJobManager().runJobLocal(jobContext, new JobRunnable() {
				
				@Override
				public void run(TaskLogger jobLogger) {
					notifyJobRunning(jobContext.getBuildId(), agentId);
					
					var agentData = getSessionManager().call(
							() -> getAgentManager().load(agentId).getAgentData());

					agentSession = getAgentManager().getAgentSession(agentId);
					if (agentSession == null)
						throw new ExplicitException("Allocated agent not connected to current server, please retry later");

					jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...",
							getName(), agentData.getName()));

					List<Map<String, String>> registryLogins = new ArrayList<>();
					for (RegistryLogin login : getRegistryLogins()) {
						registryLogins.add(CollectionUtils.newHashMap(
								"url", login.getRegistryUrl(),
								"userName", login.getUserName(),
								"password", login.getPassword()));
					}

					List<Map<String, Serializable>> services = new ArrayList<>();
					for (Service service : jobContext.getServices())
						services.add(service.toMap());

					String jobToken = jobContext.getJobToken();
					DockerJobData jobData = new DockerJobData(jobToken, getName(), jobContext.getProjectPath(),
							jobContext.getProjectId(), jobContext.getRefName(), jobContext.getCommitId().name(),
							jobContext.getBuildNumber(), jobContext.getActions(), jobContext.getRetried(),
							services, registryLogins, isMountDockerSock(), getDockerSockPath(),
							getCpuLimit(), getMemoryLimit(), getRunOptions());

					try {
						WebsocketUtils.call(agentSession, jobData, 0);
					} catch (InterruptedException|TimeoutException e) {
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
					}
				}

				@Override
				public void resume(JobContext jobContext) {
					if (agentSession != null )
						new Message(MessageTypes.RESUME_JOB, jobContext.getJobToken()).sendBy(agentSession);
				}

				@Override
				public Shell openShell(JobContext jobContext, Terminal terminal) {
					if (agentSession != null)
						return new AgentShell(terminal, agentSession, jobContext.getJobToken());
					else
						throw new ExplicitException("Shell not ready");
				}
				
			});
		};

		jobLogger.log("Pending resource allocation...");
		getResourceAllocator().runAgentJob(
				AgentQuery.parse(agentQuery, true), getName(), getConcurrencyNumber(),
				jobContext.getServices().size()+1, runnable);
	}
	
	private LogManager getLogManager() {
		return OneDev.getInstance(LogManager.class);
	}
	
	private ClusterManager getClusterManager() {
		return OneDev.getInstance(ClusterManager.class);
	}
	
	private ResourceAllocator getResourceAllocator() {
		return OneDev.getInstance(ResourceAllocator.class);
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		String jobToken = UUID.randomUUID().toString();
		getLogManager().addJobLogger(jobToken, jobLogger);
		try {
			UUID localServerUUID = getClusterManager().getLocalServerUUID();
			jobLogger.log("Pending resource allocation...");
			
			AgentRunnable runnable = agentId -> {
				TaskLogger currentJobLogger = new TaskLogger() {

					@Override
					public void log(String message, String sessionId) {
						getClusterManager().runOnServer(
								localServerUUID,
								new LogTask(jobToken, message, sessionId));
					}

				};

				var agentData = getSessionManager().call(
						() -> getAgentManager().load(agentId).getAgentData());

				Session agentSession = getAgentManager().getAgentSession(agentId);
				if (agentSession == null)
					throw new ExplicitException("Allocated agent not connected to current server, please retry later");

				currentJobLogger.log(String.format("Testing on agent '%s'...", agentData.getName()));

				List<Map<String, String>> registryLogins = new ArrayList<>();
				for (RegistryLogin login: getRegistryLogins()) {
					registryLogins.add(CollectionUtils.newHashMap(
							"url", login.getRegistryUrl(),
							"userName", login.getUserName(),
							"password", login.getPassword()));
				}

				TestDockerJobData jobData = new TestDockerJobData(getName(), jobToken,
						testData.getDockerImage(), registryLogins, getRunOptions());

				if (getLogManager().getJobLogger(jobToken) == null) {
					getLogManager().addJobLogger(jobToken, currentJobLogger);
					try {
						WebsocketUtils.call(agentSession, jobData, 0);
					} catch (InterruptedException | TimeoutException e) {
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
					} finally {
						getLogManager().removeJobLogger(jobToken);
					}
				} else {
					try {
						WebsocketUtils.call(agentSession, jobData, 0);
					} catch (InterruptedException | TimeoutException e) {
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
					}
				}
			};
			
			getResourceAllocator().runAgentJob(
					AgentQuery.parse(agentQuery, true), getName(), 
					getConcurrencyNumber(), 1, runnable);
		} finally {
			getLogManager().removeJobLogger(jobToken);
		}
	}

	@Override
	public String getDockerExecutable() {
		return super.getDockerExecutable();
	}

}