package io.onedev.server.storage;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Build;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

@Singleton
public class DefaultStorageManager implements StorageManager {

	private final ProjectManager projectManager;
	
	private final BuildManager buildManager;
	
	@Inject
	public DefaultStorageManager(ProjectManager projectManager, BuildManager buildManager) {
		this.projectManager = projectManager;
		this.buildManager = buildManager;
	}
	
	@Override
	public void initLfsDir(Long projectId) {
		FileUtils.createDir(new File(projectManager.getGitDir(projectId), "lfs"));
	}

	@Override
	public void initArtifactsDir(Long projectId, Long buildNumber) {
		File buildDir = buildManager.getStorageDir(projectId, buildNumber);
		FileUtils.createDir(new File(buildDir, Build.ARTIFACTS_DIR));
	}
	
}
