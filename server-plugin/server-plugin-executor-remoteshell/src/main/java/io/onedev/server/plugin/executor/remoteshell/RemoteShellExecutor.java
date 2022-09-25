package io.onedev.server.plugin.executor.remoteshell;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
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
import io.onedev.server.buildspec.job.JobContext;
import io.onedev.server.job.resource.AgentAwareRunnable;
import io.onedev.server.job.resource.ResourceManager;
import io.onedev.server.plugin.executor.servershell.ServerShellExecutor;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.tasklog.JobLogManager;
import io.onedev.server.terminal.RemoteSession;
import io.onedev.server.terminal.ShellSession;
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
	public void execute(JobContext jobContext) {
		AgentQuery parsedQeury = AgentQuery.parse(agentQuery, true);
		TaskLogger jobLogger = jobContext.getLogger();
		OneDev.getInstance(ResourceManager.class).run(new AgentAwareRunnable() {

			@Override
			public void runOn(Long agentId, Session agentSession, AgentData agentData) {
				jobLogger.log(String.format("Executing job (executor: %s, agent: %s)...", getName(), agentData.getName()));
				jobContext.notifyJobRunning(agentId);

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
				
				RemoteShellExecutor.this.agentSession = agentSession;
				try {
					WebsocketUtils.call(agentSession, jobData, 0);
				} catch (InterruptedException | TimeoutException e) {
					new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
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
						new Message(MessageTypes.CANCEL_JOB, jobToken).sendBy(agentSession);
					} 
					
				}
				
			}, new HashMap<>(), parsedQeury, new HashMap<>(), jobLogger);
		} finally {
			logManager.deregisterLogger(jobToken);
		}
	}

	@Override
	public void resume(JobContext jobContext) {
		if (agentSession != null) 
			new Message(MessageTypes.RESUME_JOB, jobContext.getJobToken()).sendBy(agentSession);
	}

	@Override
	public ShellSession openShell(IWebSocketConnection connection, JobContext jobContext) {
		if (agentSession != null) 
			return new RemoteSession(connection, agentSession, jobContext.getJobToken());
		else
			throw new ExplicitException("Shell not ready");
	}

}