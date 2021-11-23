package io.onedev.server.plugin.executor.remoteshell;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;

import io.onedev.agent.AgentData;
import io.onedev.agent.Message;
import io.onedev.agent.MessageType;
import io.onedev.agent.WebsocketUtils;
import io.onedev.agent.job.ShellJobData;
import io.onedev.agent.job.TestShellJobData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.CacheSpec;
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.job.resource.AgentAwareRunnable;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.tasklog.JobLogManager;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Horizontal;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;

@Editable(order=500, name="Remote Shell Executor", description="This executor runs build jobs with remote machines's shell facility via <a href='/administration/agents' target='_blank'>agents</a>")
@Horizontal
public class RemoteShellExecutor extends ServerShellExecutor {

	private static final long serialVersionUID = 1L;
	
	private String agentQuery;
	
	@Editable(order=390, name="Agent Selector", description="Specify agents applicable for this executor")
	@io.onedev.server.web.editable.annotation.AgentQuery(forExecutor=true)
	@NameOfEmptyValue("Any agent")
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

				for (CacheSpec cacheSpec: jobContext.getCacheSpecs()) {
					if (new File(cacheSpec.getPath()).isAbsolute()) {
						throw new ExplicitException("Shell executor does not support "
								+ "absolute cache path: " + cacheSpec.getPath());
					}
				}
				
				List<String> trustCertContent = getTrustCertContent();
				ShellJobData jobData = new ShellJobData(jobToken, getName(), jobContext.getProjectPath(), 
						jobContext.getProjectId(), jobContext.getCommitId().name(), jobContext.getBuildNumber(), 
						jobContext.getActions(), trustCertContent);
				
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
	
					TestShellJobData jobData = new TestShellJobData(jobToken, testData.getCommands());
					
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

}