package io.onedev.server.workspace;

import java.io.File;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.agent.AgentUtils;
import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.k8shelper.WorkspaceHelper;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.git.GitUtils;
import io.onedev.server.persistence.TransactionService;

public final class ServerProvisionerUtils {

	private static final Logger logger = LoggerFactory.getLogger(ServerProvisionerUtils.class);

	private static final String WORKSPACES_DIR = "workspaces";

	private static final String WORK_DIR = "work";

	public static File getWorkspaceDir(Long projectId, Long workspaceNumber) {
		var workspacesDir = new File(Bootstrap.getSiteDir(), WORKSPACES_DIR);
		return new File(workspacesDir, projectId + "/" + workspaceNumber);
	}

	public static File getWorkspaceDir(WorkspaceContext context) {
		return getWorkspaceDir(context.getProjectId(), context.getWorkspaceNumber());
	}

	public static File getWorkDir(Long projectId, Long workspaceNumber) {
		return new File(getWorkspaceDir(projectId, workspaceNumber), WORK_DIR);
	}

	public static File getWorkDir(WorkspaceContext context) {
		return getWorkDir(context.getProjectId(), context.getWorkspaceNumber());
	}

	public static void deleteWorkspace(Long projectId, Long workspaceNumber,
			@Nullable String pinnedServer) {
		if (pinnedServer != null) {
			OneDev.getInstance(ClusterService.class).runOnServer(pinnedServer, () -> {
				var workspaceDir = getWorkspaceDir(projectId, workspaceNumber);
				if (workspaceDir.exists())
					FileUtils.deleteDir(workspaceDir);
				return null;
			});
		} else {
			logger.warn("Provision server unknown. Skipping cleanup workspace (project id: {}, workspace number: {})",
					projectId, workspaceNumber);
		}
	}

	public static void persistServerAddress(Long workspaceId, String serverAddress) {
		OneDev.getInstance(TransactionService.class).run(() -> {
			var workspaceService = OneDev.getInstance(WorkspaceService.class);
			var workspace = workspaceService.load(workspaceId);
			if (!Objects.equals(workspace.getServerAddress(), serverAddress)) {
				workspace.setServerAddress(serverAddress);
				workspaceService.update(workspace);
			}
		});
	}

	public static void setupRepository(WorkspaceContext context, String runtimeWorkspaceDirPath,
			TaskLogger logger) {
		var workspaceDir = getWorkspaceDir(context);
		var cloneInfo = context.getCloneInfo();

		WorkspaceHelper.setupRepository(workspaceDir, GitUtils.newGit(), context.getUserName(),
				context.getUserEmail(), cloneInfo, context.getRefName(),
				context.getSpec().isRetrieveLfs(), Bootstrap.getTrustCertsDir(),
				runtimeWorkspaceDirPath, context.getProjectGitDir(), 
				AgentUtils.newInfoLogger(logger), AgentUtils.newWarningLogger(logger));
	}
	
}
