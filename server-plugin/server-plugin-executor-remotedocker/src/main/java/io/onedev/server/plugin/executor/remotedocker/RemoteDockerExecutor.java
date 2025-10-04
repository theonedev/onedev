package io.onedev.server.plugin.executor.remotedocker;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.job.DockerJobData;
import io.onedev.agent.job.TestDockerJobData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Numeric;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.SettingService;
import io.onedev.server.job.*;
import io.onedev.server.job.log.LogService;
import io.onedev.server.job.log.ServerJobLogger;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.terminal.AgentShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static io.onedev.agent.WebsocketUtils.call;
import static java.util.stream.Collectors.toList;

@Editable(order=210, description="This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>")
public class RemoteDockerExecutor extends ServerDockerExecutor {

	private static final long serialVersionUID = 1L;
	
	private String agentQuery;

	private transient volatile Session agentSession;
	
	@Editable(order=390, name="Agent Selector", placeholder="Any agent", 
			description="Specify agents applicable for this executor")
	@io.onedev.server.annotation.AgentQuery(forExecutor=true)
	public String getAgentQuery() {
		return agentQuery;
	}

	public void setAgentQuery(String agentQuery) {
		this.agentQuery = agentQuery;
	}

	@Editable(order=450, description = "" +
			"Specify max number of jobs/services this executor can run concurrently " +
			"on each matched agent. Leave empty to set as agent CPU cores")
	@Numeric
	@Override
	public String getConcurrency() {
		return super.getConcurrency();
	}

	@Override
	public void setConcurrency(String concurrency) {
		super.setConcurrency(concurrency);
	}
	
	private AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}
	
	private JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}

	private SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}
	
	private int getConcurrencyNumber() {
		if (getConcurrency() != null)
			return Integer.parseInt(getConcurrency());
		else
			return 0;		
	}
	
	@Override
	public boolean execute(JobContext jobContext, TaskLogger logger) {
		AgentRunnable runnable = (agentId) -> {
			return getJobService().runJob(jobContext, new JobRunnable() {
				
				@Override
				public boolean run(TaskLogger jobLogger) {
					notifyJobRunning(jobContext.getBuildId(), agentId);
					
					var agentData = getSessionService().call(() -> getAgentService().load(agentId).getAgentData());

					agentSession = getAgentService().getAgentSession(agentId);
					if (agentSession == null)
						throw new ExplicitException("Allocated agent not connected to current server, please retry later");

					jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...",
							getName(), agentData.getName()));

					String jobToken = jobContext.getJobToken();
					var registryLogins = getRegistryLogins().stream().map(it->it.getFacade(jobToken)).collect(toList());
					
					DockerJobData jobData = new DockerJobData(jobToken, getName(), jobContext.getProjectPath(),
							jobContext.getProjectId(), jobContext.getRefName(), jobContext.getCommitId().name(),
							jobContext.getBuildNumber(), jobContext.getSubmitSequence(), jobContext.getActions(),
							jobContext.getServices(), registryLogins, isMountDockerSock(), getDockerSockPath(), 
							getDockerBuilder(), getCpuLimit(), getMemoryLimit(), getRunOptions(), 
							getNetworkOptions(), isAlwaysPullImage(), jobContext.getSecretMasker());

					try {
						return call(agentSession, jobData, jobContext.getTimeout()*1000L);
					} catch (InterruptedException|TimeoutException e) {
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
						throw new RuntimeException(e);
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

		logger.log("Pending resource allocation...");
		return getResourceAllocator().runAgentJob(
				AgentQuery.parse(agentQuery, true), getName(), getConcurrencyNumber(),
				jobContext.getServices().size()+1, runnable);
	}
	
	private LogService getLogService() {
		return OneDev.getInstance(LogService.class);
	}
	
	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
	
	private ResourceAllocator getResourceAllocator() {
		return OneDev.getInstance(ResourceAllocator.class);
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		String jobToken = UUID.randomUUID().toString();
		getLogService().addJobLogger(jobToken, jobLogger);
		try {
			String testServer = getClusterService().getLocalServerAddress();
			jobLogger.log("Pending resource allocation...");
			AgentRunnable runnable = agentId -> {
				TaskLogger currentJobLogger = new ServerJobLogger(testServer, jobToken);
				var agentData = getSessionService().call(
						() -> getAgentService().load(agentId).getAgentData());

				Session agentSession = getAgentService().getAgentSession(agentId);
				if (agentSession == null)
					throw new ExplicitException("Allocated agent not connected to current server, please retry later");

				currentJobLogger.log(String.format("Testing on agent '%s'...", agentData.getName()));

				TestDockerJobData jobData = new TestDockerJobData(getName(), jobToken,
						testData.getDockerImage(), getDockerSockPath(), getRegistryLogins(jobToken), 
						OneDev.getInstance(SettingService.class).getSystemSetting().getServerUrl(),
						getRunOptions());

				long timeout = 300*1000L;
				if (getLogService().getJobLogger(jobToken) == null) {
					getLogService().addJobLogger(jobToken, currentJobLogger);
					try {
						return call(agentSession, jobData, timeout);
					} catch (InterruptedException | TimeoutException e) {
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
						throw new RuntimeException(e);
					} finally {
						getLogService().removeJobLogger(jobToken);
					}
				} else {
					try {
						return call(agentSession, jobData, timeout);
					} catch (InterruptedException | TimeoutException e) {
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
						throw new RuntimeException(e);
					}
				}
			};

			getResourceAllocator().runAgentJob(
					AgentQuery.parse(agentQuery, true), getName(), 
					getConcurrencyNumber(), 1, runnable);
		} finally {
			getLogService().removeJobLogger(jobToken);
		}
	}

	@Override
	public String getDockerExecutable() {
		return super.getDockerExecutable();
	}

}