package io.onedev.server.agent;

import static io.onedev.agent.WebsocketUtils.call;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.api.Session;
import org.jspecify.annotations.Nullable;

import io.onedev.agent.TestData;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.logging.LogService;
import io.onedev.server.logging.ServerLogger;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.ResourceService;

public class AgentHelper {

	public static void test(@Nullable String agentQuery, String resourceName, int concurrency,
			TestData testData, TaskLogger logger) {
		var token = testData.getToken();
		getLogService().addLogger(token, logger);
		try {
			String testServer = getClusterService().getLocalServerAddress();
			logger.log("Pending resource allocation...");
			getResourceService().submitAgentTask(
					null,
					AgentQuery.parse(agentQuery, true),
					resourceName,
					concurrency,
					1,
					agentId -> {
						TaskLogger currentLogger = new ServerLogger(testServer, token);
						var agentData = getSessionService().call(
								() -> getAgentService().load(agentId).getAgentData());

						Session agentSession = getAgentService().getAgentSession(agentId);
						if (agentSession == null) {
							throw new ExplicitException(
									"Allocated agent not connected to current server, please retry later");
						}

						currentLogger.log(String.format("Testing on agent '%s'...", agentData.getName()));

						if (getLogService().getLogger(token) == null) {
							getLogService().addLogger(token, currentLogger);
							try {
								return testCallAgent(agentSession, testData, token);
							} finally {
								getLogService().removeLogger(token);
							}
						} else {
							return testCallAgent(agentSession, testData, token);
						}
					}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		} finally {
			getLogService().removeLogger(token);
		}
	}

	private static Void testCallAgent(Session agentSession, Serializable testData, String testToken) {
		try {
			call(agentSession, testData);
			return null;
		} catch (InterruptedException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	private static AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}

	private static ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	private static LogService getLogService() {
		return OneDev.getInstance(LogService.class);
	}

	private static ResourceService getResourceService() {
		return OneDev.getInstance(ResourceService.class);
	}

	private static SessionService getSessionService() {
		return OneDev.getInstance(SessionService.class);
	}

}
