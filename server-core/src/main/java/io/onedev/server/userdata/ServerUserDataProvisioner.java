package io.onedev.server.userdata;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.TarUtils;
import io.onedev.k8shelper.UserDataFacade;
import io.onedev.k8shelper.UserDataProvisioner;
import io.onedev.server.OneDev;
import io.onedev.server.model.User;
import io.onedev.server.service.UserService;
import io.onedev.server.util.PathIndexUtils;

public class ServerUserDataProvisioner extends UserDataProvisioner {

	private final Long userId;

	public ServerUserDataProvisioner(Long userId, List<UserDataFacade> userDatas) {
		super(userDatas);
		this.userId = userId;
	}

	@Nullable
	public String getInitEntrypointArgs(String workspaceContainerPath) {
		var storedPaths = new HashSet<String>();
		for (var userData : userDatas) {
			var key = userData.getKey();
			var dataStorageDir = getUserService().getWorkspaceDataDir(userId, key, false);
			var lockName = User.getWorkspaceDataLockName(userId, key);
			LockUtils.read(lockName, () -> {
				var pathIndexes = PathIndexUtils.read(dataStorageDir);
				for (var entry : userData.getEntries()) {
					var path = entry.getPath();
					var pathIndex = pathIndexes.get(path);
					if (pathIndex != null && new File(dataStorageDir, String.valueOf(pathIndex)).exists())
						storedPaths.add(path);
				}
			});
		}

		var initPaths = new HashSet<String>();
		for (var path : pathIndexes.keySet()) {
			if (!storedPaths.contains(path))
				initPaths.add(path);
		}

		var initEntrypointArgs = new StringBuilder();
		var userDataContainerPath = workspaceContainerPath + "/" + USER_DATA_DIR;
		for (var entry : pathIndexes.entrySet()) {
			var path = entry.getKey();
			var pathIndex = entry.getValue();
			if (initPaths.contains(path)) {
				var targetPath = workspaceContainerPath + "/" + getSubPath(pathIndex);
				initEntrypointArgs.append(" ; ");
				initEntrypointArgs.append("if [ ! -e ").append(targetPath).append(" ]; then ");
				initEntrypointArgs.append("if [ -e '").append(path).append("' ]; then ");
				initEntrypointArgs.append("cp -a '").append(path).append("' ").append(targetPath);
				initEntrypointArgs.append("; else mkdir -p ").append(targetPath).append("; fi; fi");
			}
		}
		if (initEntrypointArgs.length() > 0)
			return "set -e ; mkdir -p " + userDataContainerPath + initEntrypointArgs;
		else
			return null;
	}

	@Override
	protected void download(String key, String path, File pathFile) {
		getUserService().downloadWorkspaceData(userId, key, path, is -> TarUtils.untar(is, pathFile, false));
	}

	@Override
	protected void upload(String key, String path, File pathFile, List<String> excludes) {
		getUserService().uploadWorkspaceData(userId, key, path, os -> TarUtils.tar(pathFile, excludes, os, false));
	}

	@Override
	protected void notifyUploaded(String key) {
		getUserService().notifyWorkspaceDataUploaded(userId, key);
	}

	private static UserService getUserService() {
		return OneDev.getInstance(UserService.class);
	}
	
}
