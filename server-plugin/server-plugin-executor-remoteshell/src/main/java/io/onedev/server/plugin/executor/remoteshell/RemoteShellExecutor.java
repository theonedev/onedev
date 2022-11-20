package io.onedev.server.plugin.executor.remoteshell;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.AgentData;
import io.onedev.agent.Message;
import io.onedev.agent.MessageTypes;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.ShellJobData;
import io.onedev.agent.job.TestShellJobData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.job.AgentInfo;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.job.ResourceRunnable;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.LogTask;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.terminal.AgentShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;

@Editable(order=500, name="Remote Shell Executor", description=""
		+ "This executor runs build jobs with remote machines's shell facility via <a href='/administration/agents' target='_blank'>agents</a><br>"
		+ "<b class='text-danger'>WARNING</b>: Jobs running with this executor has same permission "
		+ "as corresponding agent process. Make sure it can only be used by trusted jobs via job "
		+ "authorization setting")
@Horizontal
public class RemoteShellExecutor extends ServerShellExecutor {

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

	@Override
	public AgentQuery getAgentRequirement() {
		return AgentQuery.parse(agentQuery, true);		
	}
	
	@Override
	public void execute(JobContext jobContext, TaskLogger jobLogger, AgentInfo agentInfo) {
		jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...", 
				getName(), agentInfo.getData().getName()));

		if (!jobContext.getServices().isEmpty()) {
			throw new ExplicitException("This job requires services, which can only be supported "
					+ "by docker aware executors");
		}
		
		for (CacheSpec cacheSpec: jobContext.getCacheSpecs()) {
			if (new File(cacheSpec.getPath()).isAbsolute()) {
				throw new ExplicitException("Shell executor does not support "
						+ "absolute cache path: " + cacheSpec.getPath());
			}
		}
		
		String jobToken = jobContext.getJobToken();
		List<String> trustCertContent = getTrustCertContent();
		ShellJobData jobData = new ShellJobData(jobToken, getName(), jobContext.getProjectPath(), 
				jobContext.getProjectId(), jobContext.getRefName(), jobContext.getCommitId().name(), 
				jobContext.getBuildNumber(), jobContext.getActions(), trustCertContent);

		agentSession = agentInfo.getSession();
		try {
			WebsocketUtils.call(agentSession, jobData, 0);
		} catch (InterruptedException | TimeoutException e) {
			new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
		}
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
		UUID localServerUUID = getClusterManager().getLocalServerUUID();
		getLogManager().addJobLogger(jobToken, jobLogger);
		try {
			jobLogger.log("Waiting for resources...");
			getResourceAllocator().run(
					new TestRunnable(jobToken, this, testData, localServerUUID), 
					getAgentRequirement(), new HashMap<>());
		} finally {
			getLogManager().removeJobLogger(jobToken);
		}
	}

	private void testLocal(String jobToken, AgentInfo agentInfo, 
			TestData testData, UUID dispatcherMemberUUID) {
		TaskLogger jobLogger = new TaskLogger() {

			@Override
			public void log(String message, String sessionId) {
				getClusterManager().runOnServer(
						dispatcherMemberUUID, 
						new LogTask(jobToken, message, sessionId));
			}
			
		};
		
		AgentData agentData = agentInfo.getData();
		Session agentSession = agentInfo.getSession();
		jobLogger.log(String.format("Testing on agent '%s'...", agentData.getName()));
		
		TestShellJobData jobData = new TestShellJobData(jobToken, testData.getCommands());

		if (getLogManager().getJobLogger(jobToken) == null) {
			getLogManager().addJobLogger(jobToken, jobLogger);
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

	private static class TestRunnable implements ResourceRunnable {

		private static final long serialVersionUID = 1L;

		private final String jobToken;
		
		private final RemoteShellExecutor jobExecutor;
		
		private final TestData testData;
		
		private final UUID dispatcherServerUUID;
		
		public TestRunnable(String jobToken, RemoteShellExecutor jobExecutor, 
				TestData testData, UUID dispatcherServerUUID) {
			this.jobToken = jobToken;
			this.jobExecutor = jobExecutor;
			this.testData = testData;
			this.dispatcherServerUUID = dispatcherServerUUID;
		}
		
		@Override
		public void run(AgentInfo agentInfo) {
			jobExecutor.testLocal(jobToken, agentInfo, testData, dispatcherServerUUID);
		}
		
	}
	
}