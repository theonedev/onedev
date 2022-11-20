package io.onedev.server.plugin.executor.remotedocker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.AgentData;
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
import io.onedev.server.job.AgentInfo;
import io.onedev.server.job.JobContext;
import io.onedev.server.job.ResourceAllocator;
import io.onedev.server.job.ResourceRunnable;
import io.onedev.server.job.log.LogManager;
import io.onedev.server.job.log.LogTask;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.terminal.AgentShell;
import io.onedev.server.terminal.Shell;
import io.onedev.server.terminal.Terminal;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=210, description="This executor runs build jobs as docker containers on remote machines via <a href='/administration/agents' target='_blank'>agents</a>")
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

	@Override
	public AgentQuery getAgentRequirement() {
		return AgentQuery.parse(agentQuery, true);
	}
	
	@Override
	public void execute(JobContext jobContext, TaskLogger jobLogger, AgentInfo agentInfo) {
		jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...", 
				getName(), agentInfo.getData().getName()));

		List<Map<String, String>> registryLogins = new ArrayList<>();
		for (RegistryLogin login: getRegistryLogins()) {
			registryLogins.add(CollectionUtils.newHashMap(
					"url", login.getRegistryUrl(), 
					"userName", login.getUserName(), 
					"password", login.getPassword()));
		}
		
		List<Map<String, Serializable>> services = new ArrayList<>();
		for (Service service: jobContext.getServices())
			services.add(service.toMap());

		String jobToken = jobContext.getJobToken();
		List<String> trustCertContent = getTrustCertContent();
		DockerJobData jobData = new DockerJobData(jobToken, getName(), jobContext.getProjectPath(), 
				jobContext.getProjectId(), jobContext.getRefName(), jobContext.getCommitId().name(), 
				jobContext.getBuildNumber(), jobContext.getActions(), jobContext.getRetried(), 
				services, registryLogins, isMountDockerSock(), getDockerSockPath(), trustCertContent, getRunOptions());
		
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
		getLogManager().addJobLogger(jobToken, jobLogger);
		try {
			UUID localServerUUID = getClusterManager().getLocalServerUUID();
			jobLogger.log("Waiting for resources...");
			getResourceAllocator().run(
					new TestRunnable(jobToken, this, testData, localServerUUID), 
					getAgentRequirement(), new HashMap<>());
		} finally {
			getLogManager().removeJobLogger(jobToken);
		}
	}

	private void testLocal(String jobToken, AgentInfo agentInfo, 
			TestData testData, UUID dispatcherServerUUID) {
		TaskLogger jobLogger = new TaskLogger() {

			@Override
			public void log(String message, String sessionId) {
				getClusterManager().runOnServer(
						dispatcherServerUUID, 
						new LogTask(jobToken, message, sessionId));
			}
			
		};
		
		AgentData agentData = agentInfo.getData();
		Session agentSession = agentInfo.getSession();
		
		jobLogger.log(String.format("Testing on agent '%s'...", agentData.getName()));

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

	@Override
	public String getDockerExecutable() {
		return super.getDockerExecutable();
	}

	private static class TestRunnable implements ResourceRunnable {

		private static final long serialVersionUID = 1L;

		private final String jobToken;
		
		private final RemoteDockerExecutor jobExecutor;
		
		private final TestData testData;
		
		private final UUID dispatcherServerUUID;
		
		public TestRunnable(String jobToken, RemoteDockerExecutor jobExecutor, 
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