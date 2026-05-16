package io.onedev.server.plugin.executor.remoteshell;

import static io.onedev.agent.WebsocketUtils.call;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.job.JobResumeData;
import io.onedev.agent.job.ShellJobData;
import io.onedev.agent.job.TestShellJobData;
import io.onedev.agent.shell.JobShellOpenData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.agent.AgentHelper;
import io.onedev.server.annotation.Editable;
import io.onedev.server.job.JobAgentShell;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.JobRunnable;
import io.onedev.server.job.JobService;
import io.onedev.server.job.JobTerminal;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.ResourceService;
import io.onedev.server.service.support.AgentCallable;
import io.onedev.server.terminal.Shell;

@Editable(order=RemoteShellExecutor.ORDER, name="Remote Shell Executor", description=""
		+ "This executor runs build jobs with remote machines's shell facility via <a href='/~administration/agents' target='_blank'>agents</a>")
public class RemoteShellExecutor extends ServerShellExecutor {

	private static final long serialVersionUID = 1L;

	static final int ORDER = 600;
	
	private String agentQuery;
	
	private transient volatile Session agentSession;
	
	@Editable(order=390, name="Agent Selector", placeholder="Any agent", 
			description="Specify agents applicable for this executor")
	@io.onedev.server.annotation.AgentQuery(forRunner=true)
	public String getAgentQuery() {
		return agentQuery;
	}

	public void setAgentQuery(String agentQuery) {
		this.agentQuery = agentQuery;
	}

	@Editable(order=1000, description = "Specify max number of jobs this executor can run " +
			"concurrently on each matched agent. Leave empty to set as agent CPU cores")
	@Override
	public Integer getConcurrency() {
		return super.getConcurrency();
	}

	@Override
	public void setConcurrency(Integer concurrency) {
		super.setConcurrency(concurrency);
	}
	
	@Override
	public boolean execute(JobContext jobContext, TaskLogger jobLogger) {
		AgentCallable<Boolean> runnable = (agentId) -> getJobService().runJob(jobContext, new JobRunnable() {

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
				if (agentSession != null) {
					var resumeData = new JobResumeData(jobContext.getProjectId(), jobContext.getBuildNumber(),
							jobContext.getSubmitSequence());
					new Message(MessageTypes.RESUME_JOB, resumeData).sendBy(agentSession);
				}
			}

			@Override
			public Shell openShell(JobContext jobContext, JobTerminal terminal) {
				if (agentSession != null) {
					var shellOpenData = new JobShellOpenData(false, jobContext.getJobToken(), 
							terminal.getSessionId(), jobContext.getProjectId(), 
							jobContext.getBuildNumber(), jobContext.getSubmitSequence(), null);
					return new JobAgentShell(terminal, agentSession, shellOpenData);
				} else {
					throw new ExplicitException("Shell not ready");
				}
			}
			
		});

		jobLogger.log("Pending resource allocation...");
		try {
			return getResourceService().submitAgentTask(null, AgentQuery.parse(agentQuery, true), 
					getName(), getConcurrencyNumber(), 1, runnable).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	public JobService getJobService() {
		return OneDev.getInstance(JobService.class);
	}
	
	private ResourceService getResourceService() {
		return OneDev.getInstance(ResourceService.class);
	}

	private AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}
	
	private SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}
	
	@Override
	public void test(None testData, TaskLogger jobLogger) {
		AgentHelper.test(agentQuery, getName(), getConcurrencyNumber(),
				new TestShellJobData(UUID.randomUUID().toString()), jobLogger);
	}

}