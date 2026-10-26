package io.onedev.server.workspace;

import static io.onedev.agent.WebsocketUtils.call;
import static io.onedev.agent.workspace.StopWorkspaceRequest.TIMEOUT_MILLIS;

import java.io.Serializable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import org.eclipse.jetty.websocket.api.Session;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.agent.workspace.ProvisionMode;
import io.onedev.agent.workspace.StopWorkspaceRequest;
import io.onedev.agent.workspace.WorkspaceDeleteRequest;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.search.entity.agent.AgentQuery;
import io.onedev.server.service.AgentService;
import io.onedev.server.service.ResourceService;

public final class AgentProvisionerUtils {

	private static final Logger logger = LoggerFactory.getLogger(AgentProvisionerUtils.class);

	private static final ThreadLocal<Long> ALLOCATED_AGENT = new ThreadLocal<>();

	public static Long getAllocatedAgent() {
		return ALLOCATED_AGENT.get();
	}

	/** Call the workspace agent, stopping the workspace if interrupted or timed out. */
	public static <T extends Serializable> T callWorkspaceAgent(Session session, String token,
			Serializable request) {
		return callWorkspaceAgent(session, token, request, null);
	}

	/**
	 * Call the workspace agent, stopping the workspace if interrupted or timed out.
	 * Run the optional cancellation callback after attempting to stop the workspace.
	 */
	public static <T extends Serializable> T callWorkspaceAgent(Session session, String token,
			Serializable request, @Nullable Runnable onCancel) {
		return callWorkspaceAgent(session, token, request, onCancel, () -> false);
	}

	public static <T extends Serializable> T callWorkspaceAgent(Session session, String token,
			Serializable request, @Nullable Runnable onCancel, BooleanSupplier uploadData) {
		try {
			return call(session, request, 0L);
		} catch (InterruptedException | TimeoutException e) {
			try {
				call(session, new StopWorkspaceRequest(token, uploadData.getAsBoolean()), TIMEOUT_MILLIS);
			} catch (Exception e2) {
				logger.error("Error stopping workspace", e2);
			} finally {
				if (onCancel != null)
					onCancel.run();
			}
			throw new RuntimeException(e);
		}
	}

	public static <T> Future<T> submitTask(@Nullable String pinnedServerAddress, @Nullable Long pinnedAgentId,
			@Nullable String agentQuery, String resourceName, int concurrency,
			String cannotReprovisionVia, ClusterTask<T> task, TaskLogger logger) {
		if (pinnedServerAddress != null) {
			throw new ExplicitException(String.format("""
				This workspace is provisioned on server previously, \
				and cannot be reprovisioned via %s""", cannotReprovisionVia));
		}

		logger.log("Pending resource allocation...");
		return getResourceService().submitAgentTask(
				pinnedAgentId,
				AgentQuery.parse(agentQuery, true),
				resourceName,
				concurrency, 1,
				agentId -> {
					ALLOCATED_AGENT.set(agentId);
					try {
						return task.call();
					} catch (Exception e) {
						throw ExceptionUtils.unchecked(e);
					} finally {
						ALLOCATED_AGENT.remove();
					}
				});
	}

	public static void deleteWorkspace(ProvisionMode provisionMode, Long projectId, Long workspaceNumber,
			@Nullable Long pinnedAgentId) {
		if (pinnedAgentId == null) {
			logger.warn(
					"Workspace agent unknown. Skipping deleting workspace storage (project id: {}, workspace number: {})",
					projectId, workspaceNumber);
			return;
		}

		Session session = getAgentSession(pinnedAgentId, false);
		if (session == null) {
			logger.warn(
					"Workspace agent offline. Skipping deleting workspace storage (project id: {}, workspace number: {})",
					projectId, workspaceNumber);
			return;
		}

		try {
			call(session, new WorkspaceDeleteRequest(provisionMode, projectId, workspaceNumber));
		} catch (InterruptedException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}

	public static void persistAgent(Long workspaceId, Long agentId) {
		OneDev.getInstance(TransactionService.class).run(() -> {
			var workspaceService = OneDev.getInstance(WorkspaceService.class);
			var workspace = workspaceService.load(workspaceId);
			var agent = getAgentService().load(agentId);
			if (!agent.equals(workspace.getAgent())) {
				workspace.setAgent(agent);
				workspaceService.update(workspace);
			}
		});
	}

	@Nullable
	public static Session getAgentSession(Long agentId, boolean mustExist) {
		Session session = getAgentService().getAgentSession(agentId);
		if (session == null && mustExist)
			throw new ExplicitException("Agent disconnected");
		return session;
	}

	private static AgentService getAgentService() {
		return OneDev.getInstance(AgentService.class);
	}

	private static ResourceService getResourceService() {
		return OneDev.getInstance(ResourceService.class);
	}

}
