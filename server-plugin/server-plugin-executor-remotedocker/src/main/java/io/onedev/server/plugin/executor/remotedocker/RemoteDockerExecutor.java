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
import io.onedev.agent.MessageType;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.DockerJobData;
import io.onedev.agent.job.TestDockerJobData;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.Service;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.job.resource.AgentAwareRunnable;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.model.support.RegistryLogin;
import io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.tasklog.JobLogManager;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=210, description="This executor runs build jobs as docker containers on remote machines via <a href='/administration/agents' target='_blank'>agents</a>")
public class RemoteDockerExecutor extends ServerDockerExecutor {

	private static final long serialVersionUID = 1L;
	
	private String agentQuery;
	
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
	public void execute(String jobToken, JobContext jobContext) {
		AgentQuery parsedQeury = AgentQuery.parse(agentQuery, true);
		TaskLogger jobLogger = jobContext.getLogger();
		OneDev.getInstance(ResourceManager.class).run(new AgentAwareRunnable() {

			@Override
			public void runOn(Long agentId, Session agentSession, AgentData agentData) {
				jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...", getName(), agentData.getName()));
				jobContext.notifyJobRunning(agentId);

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
				
				List<String> trustCertContent = getTrustCertContent();
				DockerJobData jobData = new DockerJobData(jobToken, getName(), jobContext.getProjectPath(), 
						jobContext.getProjectId(), jobContext.getCommitId().name(), jobContext.getBuildNumber(), 
						jobContext.getActions(), jobContext.getRetried(), services, registryLogins, 
						trustCertContent, getRunOptions());
				
				try {
					WebsocketUtils.call(agentSession, jobData, 0);
				} catch (InterruptedException | TimeoutException e) {
					new Message(MessageType.CANCEL_JOB, jobToken).sendBy(agentSession);
				} 
				
			}
			
		}, new HashMap<>(), parsedQeury, jobContext.getResourceRequirements(), jobLogger);
		
	}

	@Override
	public void test(TestData testData, TaskLogger jobLogger) {
		JobLogManager logManager = OneDev.getInstance(JobLogManager.class);
		String jobToken = UUID.randomUUID().toString();
		logManager.registerLogger(jobToken, jobLogger);
		try {
			AgentQuery parsedQeury = AgentQuery.parse(agentQuery, true);
			
			OneDev.getInstance(ResourceManager.class).run(new AgentAwareRunnable() {
	
				@Override
				public void runOn(Long agentId, Session agentSession, AgentData agentData) {
					jobLogger.log(String.format("Testing on agent '%s'...", agentData.getName()));
	
					List<Map<String, String>> registryLogins = new ArrayList<>();
					for (RegistryLogin login: getRegistryLogins()) {
						registryLogins.add(CollectionUtils.newHashMap(
								"url", login.getRegistryUrl(), 
								"userName", login.getUserName(), 
								"password", login.getPassword()));
					}
					
					TestDockerJobData jobData = new TestDockerJobData(jobToken, testData.getDockerImage(), 
							registryLogins, getRunOptions());
					
					try {
						WebsocketUtils.call(agentSession, jobData, 0);
					} catch (InterruptedException | TimeoutException e) {
						new Message(MessageType.CANCEL_JOB, jobToken).sendBy(agentSession);
					} 
					
				}
				
			}, new HashMap<>(), parsedQeury, new HashMap<>(), jobLogger);
		} finally {
			logManager.deregisterLogger(jobToken);
		}
	}

	@Override
	public String getDockerExecutable() {
		return super.getDockerExecutable();
	}

}