package io.onedev.server.plugin.executor.remotedocker;

import static io.onedev.agent.WebsocketUtils.call;
import static java.util.stream.Collectors.toList;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.job.DockerJobData;
import io.onedev.agent.job.JobDockerSettings;
import io.onedev.agent.job.JobResumeData;
import io.onedev.agent.job.TestDockerJobData;
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
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.ResourceService;
import io.onedev.server.service.support.AgentCallable;
import io.onedev.server.terminal.Shell;

@Editable(order=RemoteDockerExecutor.ORDER, description="This executor runs build jobs as docker containers on remote machines via <a href='/~administration/agents' target='_blank'>agents</a>")
public class RemoteDockerExecutor extends ServerDockerExecutor {

	private static final long serialVersionUID = 1L;

	static final int ORDER = 500;
	
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

	@Editable(order=450, description = "" +
			"Specify max number of jobs/services this executor can run concurrently " +
			"on each matched agent. Leave empty to set as agent CPU cores")		
	@Override
	public Integer getConcurrency() {
		return super.getConcurrency();
	}

	@Override
	public void setConcurrency(Integer concurrency) {
		super.setConcurrency(concurrency);
	}

	@Override
	public String getDockerExecutable() {
		return super.getDockerExecutable();
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
		
	@Override
	public boolean execute(JobContext jobContext, TaskLogger logger) {
		AgentCallable<Boolean> runnable = (agentId) -> {
			return getJobService().runJob(jobContext, new JobRunnable() {
				
				@Override
				public boolean run(TaskLogger jobLogger) {
					notifyJobRunning(jobContext.getBuildId(), agentId);
					
					agentSession = getAgentService().getAgentSession(agentId);
					if (agentSession == null)
						throw new ExplicitException("Allocated agent not connected to current server, please retry later");

					var agentData = getSessionService().call(() -> getAgentService().load(agentId).getAgentData());

					jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...",
							getName(), agentData.getName()));

					String jobToken = jobContext.getJobToken();
					var registryLogins = getRegistryLogins().stream().map(it->it.getFacade(jobToken)).collect(toList());
					
					var dockerSettings = new JobDockerSettings(isMountDockerSock(), getDockerSockPath(),
							getCpuLimit(), getMemoryLimit(), getRunOptions(), registryLogins,
							isAlwaysPullImage(), getDockerBuilder(), getNetworkOptions());
					DockerJobData jobData = new DockerJobData(jobToken, getName(), jobContext.getProjectPath(),
							jobContext.getProjectId(), jobContext.getRefName(), jobContext.getCommitId().name(),
							jobContext.getBuildNumber(), jobContext.getSubmitSequence(), jobContext.getActions(),
							jobContext.getSecretMasker(), jobContext.getServices(), dockerSettings);

					try {
						return call(agentSession, jobData, jobContext.getTimeout()*1000L);
					} catch (InterruptedException|TimeoutException e) {
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
						var shellOpenData = new JobShellOpenData(true, jobContext.getJobToken(), 
								terminal.getSessionId(), jobContext.getProjectId(), jobContext.getBuildNumber(), 
								jobContext.getSubmitSequence(), getDockerSockPath());
						return new JobAgentShell(terminal, agentSession, shellOpenData);
					} else {
						throw new ExplicitException("Shell not ready");
					}
				}
				
			});
		};

		logger.log("Pending resource allocation...");
		try {
			return getResourceService().submitAgentTask(
					null, AgentQuery.parse(agentQuery, true), getName(),
					getConcurrencyNumber(), jobContext.getServices().size()+1, runnable).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ResourceService getResourceService() {
		return OneDev.getInstance(ResourceService.class);
	}
	
	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		var jobToken = UUID.randomUUID().toString();
		AgentHelper.test(agentQuery, getName(), getConcurrencyNumber(),
				new TestDockerJobData(
						getName(), jobToken, testData.getDockerImage(), getDockerSockPath(), 
						getRegistryLogins(jobToken), getRunOptions(), getCpuLimit(), getMemoryLimit()),
				jobLogger);
	}

}