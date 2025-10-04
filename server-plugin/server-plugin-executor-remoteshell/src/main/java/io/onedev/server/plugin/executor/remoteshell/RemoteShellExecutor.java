package io.onedev.server.plugin.executor.remoteshell;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.job.ShellJobData;
import io.onedev.agent.job.TestShellJobData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Numeric;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.service.AgentService;
import io.onedev.server.job.*;
import io.onedev.server.job.log.LogService;
import io.onedev.server.job.log.ServerJobLogger;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.terminal.AgentShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static io.onedev.agent.WebsocketUtils.call;

@Editable(order=500, name="Remote Shell Executor", description=""
		+ "This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a><br>"
		+ "<b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission as OneDev agent process. "
		+ "Make sure it can only be used by trusted jobs")
public class RemoteShellExecutor extends ServerShellExecutor {

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

	@Editable(order=1000, description = "Specify max number of jobs this executor can run " +
			"concurrently on each matched agent. Leave empty to set as agent CPU cores")
	@Numeric
	@Override
	public String getConcurrency() {
		return super.getConcurrency();
	}

	@Override
	public void setConcurrency(String concurrency) {
		super.setConcurrency(concurrency);
	}

	private int getConcurrencyNumber() {
		if (getConcurrency() != null)
			return Integer.parseInt(getConcurrency());
		else
			return 0;
	}
	
	@Override
	public boolean execute(JobContext jobContext, TaskLogger jobLogger) {
		AgentRunnable runnable = (agentId) -> getJobService().runJob(jobContext, new JobRunnable() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean run(TaskLogger jobLogger) {
				notifyJobRunning(jobContext.getBuildId(), agentId);
				
				var agentData = getSessionService().call(
						() -> getAgentService().load(agentId).getAgentData());

				agentSession = getAgentService().getAgentSession(agentId);
				if (agentSession == null)
					throw new ExplicitException("Allocated agent not connected to current server, please retry later");

				jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...",
						getName(), agentData.getName()));

				if (!jobContext.getServices().isEmpty()) {
					throw new ExplicitException("This job requires services, which can only be supported "
							+ "by docker aware executors");
				}

				String jobToken = jobContext.getJobToken();
				ShellJobData jobData = new ShellJobData(jobToken, getName(), jobContext.getProjectPath(),
						jobContext.getProjectId(), jobContext.getRefName(), jobContext.getCommitId().name(),
						jobContext.getBuildNumber(), jobContext.getSubmitSequence(), jobContext.getActions(), 
						jobContext.getSecretMasker());

				try {
					return call(agentSession, jobData, jobContext.getTimeout()*1000L);
				} catch (InterruptedException | TimeoutException e) {
					new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
					throw new RuntimeException(e);
				}
			}

			@Override
			public void resume(JobContext jobContext) {
				if (agentSession != null)
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

		jobLogger.log("Pending resource allocation...");
		return getResourceAllocator().runAgentJob(AgentQuery.parse(agentQuery, true), getName(), 
				getConcurrencyNumber(), 1, runnable);
	}
	
	private LogService getLogService() {
		return OneDev.getInstance(LogService.class);
	}
	
	private ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}
	
	public JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}
	
	private ResourceAllocator getResourceAllocator() {
		return OneDev.getInstance(ResourceAllocator.class);
	}

	private AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}
	
	private SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
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

				TestShellJobData jobData = new TestShellJobData(jobToken, testData.getCommands());

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

			getResourceAllocator().runAgentJob(AgentQuery.parse(agentQuery, true), getName(),
					getConcurrencyNumber(), 1, runnable);
		} finally {
			getLogService().removeJobLogger(jobToken);
		}
	}

}