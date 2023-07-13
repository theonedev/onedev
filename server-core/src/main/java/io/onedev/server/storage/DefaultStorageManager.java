package io.onedev.server.storage;

import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.ProjectManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

import static io.onedev.server.model.Build.getArtifactsDir;

@Singleton
public class DefaultStorageManager implements StorageManager {

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultStorageManager(ProjectManager projectManager) {
		this.projectManager = projectManager;
	}
	
	@Override
	public File initLfsDir(Long projectId) {
		var lfsDir = new File(projectManager.getGitDir(projectId), "lfs");
		FileUtils.createDir(lfsDir);
		return lfsDir;
	}

	@Override
	public File initArtifactsDir(Long projectId, Long buildNumber) {
		var artifactsDir = getArtifactsDir(projectId, buildNumber);
		FileUtils.createDir(artifactsDir);
		return artifactsDir;
	}
	
}
