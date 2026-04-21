package io.onedev.server.plugin.provisioner.docker;

import static io.onedev.commons.utils.LockUtils.read;
import static io.onedev.commons.utils.LockUtils.write;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Path;
import io.onedev.server.cluster.ClusterService;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.model.User;
import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.service.UserService;
import io.onedev.server.util.PathIndexUtils;
import io.onedev.server.util.SiteSyncUtils;
import io.onedev.server.validation.validator.PathValidator;
import io.onedev.server.workspace.WorkspaceContext;

public class UserDataProvisioner {
	
	private static final Logger logger = LoggerFactory.getLogger(UserDataProvisioner.class);

	static final String MOUNT_FILE = "onedev.this-should-be-mounted-instead";

	private final Long userId;

	private final List<UserData> userDatas;

	private final File workspaceDir;

	private final TaskLogger taskLogger;

	public UserDataProvisioner(WorkspaceContext context, File workspaceDir, TaskLogger taskLogger) {
		this.userId = context.getUserId();
		this.userDatas = context.getSpec().getUserDatas();
		this.workspaceDir = workspaceDir;
		this.taskLogger = taskLogger;
	}

	public Map<String, File> setup() {
		var pathMap = new HashMap<String, File>();
		var userService = getUserService();
		AtomicInteger cacheIndex = new AtomicInteger(1);
		for (var userData : userDatas) {
			var dataKey = userData.getKey();
			var dataStorageDir = userService.getWorkspaceDataDir(userId, dataKey, false);
			read(User.getWorkspaceDataLockName(userId, dataKey), () -> {
				try {
					var pathIndexes = PathIndexUtils.read(userService.getWorkspaceDataDir(userId, dataKey, false));
					for (var path: userData.getPaths()) {
						var pathCheckMessage = PathValidator.checkPath(Path.Type.ABSOLUTE, path);
						if (pathCheckMessage != null)
							throw new ExplicitException("Invalid user data path '" + path + "': " + pathCheckMessage);
						var pathDir = new File(workspaceDir, "user-data/" + cacheIndex.getAndIncrement());
						var storedIndex = pathIndexes.get(path);
						var pathStorageFile = storedIndex != null
								? new File(dataStorageDir, String.valueOf(storedIndex))
								: null;
						if ((!pathDir.exists() || pathDir.list().length == 0)
								&& pathStorageFile != null && pathStorageFile.exists()) {
							FileUtils.createDir(pathDir);
							try (var is = new FileInputStream(pathStorageFile)) {
								TarUtils.untar(is, pathDir, false);
							}
						}
						pathMap.put(path, pathDir);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});			
		}
		return pathMap;
	}

	private static File getUsersStorageDir() {
		return OneDev.getInstance(UserService.class).getUsersDir();
	}

	public void store(Date checkDate, Map<String, File> pathMap) {
		var usersStorageDir = getUsersStorageDir();
		var userService = getUserService();
		for (var userData: userDatas) {
			var dataKey = userData.getKey();
			var dataStorageDir = userService.getWorkspaceDataDir(userId, dataKey, true);
			var lockName = User.getWorkspaceDataLockName(userId, dataKey);
			for (var path: userData.getPaths()) {
				var pathDir = pathMap.get(path);
				if (pathDir != null && FileUtils.hasChangedFiles(pathDir, checkDate, userData.getChangeDetectionExcludes())) {
					taskLogger.log(MessageFormat.format("User data changed (key: {0}, path: {1}), storing", dataKey, path));
					write(lockName, () -> {
						try {
							var pathIndexes = PathIndexUtils.read(userService.getWorkspaceDataDir(userId, dataKey, false));
							boolean pathIndexesChanged = false;
							Integer index = pathIndexes.get(path);
							if (index == null) {
								index = pathIndexes.values().stream()
										.mapToInt(Integer::intValue).max().orElse(0) + 1;
								pathIndexes.put(path, index);
								pathIndexesChanged = true;
							}
							var pathStorageFile = new File(dataStorageDir, String.valueOf(index));
							try (var os = new FileOutputStream(pathStorageFile)) {
								TarUtils.tar(pathDir, os, false);
							}
							if (pathIndexesChanged)
								PathIndexUtils.write(userService.getWorkspaceDataDir(userId, dataKey, true), pathIndexes);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});		
				}
			}
			SiteSyncUtils.bumpVersions(usersStorageDir, dataStorageDir);
			var workspaceServer = getClusterService().getLocalServerAddress();
			getClusterService().submitToAllServers(new SyncTask(workspaceServer, userId, dataKey));
		}
	}

	private static UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}

	private static ClusterService getClusterService() {
		return OneDev.getInstance(ClusterService.class);
	}

	private static class SyncTask implements ClusterTask<Void> {

		private static final long serialVersionUID = 1L;

		private final String workspaceServer;

		private final Long userId;

		private final String dataKey;

		public SyncTask(String workspaceServer, Long userId, String dataKey) {
			this.workspaceServer = workspaceServer;
			this.userId = userId;
			this.dataKey = dataKey;
		}

		@Override
		public Void call() throws Exception {
			try {
				var localServer = getClusterService().getLocalServerAddress();
				if (!localServer.equals(workspaceServer)) {
					var usersStorageDir = getUsersStorageDir();
					var dataStorageDir = getUserService().getWorkspaceDataDir(userId, dataKey, true);
					var syncPath = Bootstrap.getSiteDir().toPath().relativize(dataStorageDir.toPath()).toString();
					var lockName = User.getWorkspaceDataLockName(userId, dataKey);					
					SiteSyncUtils.syncDirectory(workspaceServer, syncPath, 
						false, lockName, lockName);
					SiteSyncUtils.bumpVersions(usersStorageDir, dataStorageDir.getParentFile());
				}
			} catch (Exception e) {
				logger.error("Error syncing user data", e);
			}
			return null;
		}

	}
}
