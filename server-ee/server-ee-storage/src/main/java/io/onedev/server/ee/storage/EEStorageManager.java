package io.onedev.server.ee.storage;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.storage.StorageManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Files;

@Singleton
public class EEStorageManager implements StorageManager, Serializable {
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private final ProjectManager projectManager;
	
	@Inject
	public EEStorageManager(SettingManager settingManager, TransactionManager transactionManager,
							ClusterManager clusterManager, ProjectManager projectManager) { 
		this.settingManager = settingManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.projectManager = projectManager;
	}
	
	@Nullable
	private File getLfsStorageDir(Long projectId) {
		StorageSetting storageSetting = settingManager.getContributedSetting(StorageSetting.class);
		if (storageSetting != null && storageSetting.getLfsStore() != null) {
			File lfsStore = new File(storageSetting.getLfsStore());
			String projectIdString = String.valueOf(projectId);
			if (lfsStore.isAbsolute())
				return new File(lfsStore, projectIdString);
			else
				return new File(new File(Bootstrap.getSiteDir(), storageSetting.getLfsStore()), projectIdString);
		} else {
			return null;
		}
	}

	@Nullable
	private File getArtifactsStorageDir(Long projectId, @Nullable Long buildNumber) {
		StorageSetting storageSetting = settingManager.getContributedSetting(StorageSetting.class);
		if (storageSetting != null && storageSetting.getArtifactStore() != null) {
			File artifactsStore = new File(storageSetting.getArtifactStore());
			String subpath = String.valueOf(projectId);
			if (buildNumber != null)
				subpath += "/" + Build.getStoragePath(buildNumber);
			if (artifactsStore.isAbsolute())
				return new File(artifactsStore, subpath);
			else
				return new File(new File(Bootstrap.getSiteDir(), storageSetting.getArtifactStore()), subpath);
		} else {
			return null;
		}
	}

	@Override
	public void initLfsDir(Long projectId) {
		File gitDir = projectManager.getGitDir(projectId);
		File lfsStorageDir = getLfsStorageDir(projectId);
		var lfsDir = new File(gitDir, "lfs");
		if (lfsStorageDir != null && !lfsDir.exists()) {
			FileUtils.createDir(lfsStorageDir);
			try {
				Files.createSymbolicLink(lfsDir.toPath(), lfsStorageDir.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void initArtifactsDir(Long projectId, Long buildNumber) {
		File artifactsStorageDir = getArtifactsStorageDir(projectId, buildNumber);
		File artifactsDir = Build.getArtifactsDir(projectId, buildNumber);
		if (artifactsStorageDir != null && !artifactsDir.exists()) {
			FileUtils.createDir(artifactsStorageDir);
			try {
				Files.createSymbolicLink(
						artifactsDir.toPath(),
						artifactsStorageDir.toPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Build) {
			Build build = (Build) event.getEntity();
			Long projectId = build.getProject().getId();
			Long buildNumber = build.getNumber();
			String activeServer = projectManager.getActiveServer(projectId, false);
			if (activeServer != null) {
				transactionManager.runAfterCommit(() -> clusterManager.submitToServer(activeServer, () -> {
					File artifactsStorageDir = getArtifactsStorageDir(projectId, buildNumber);
					if (artifactsStorageDir != null && artifactsStorageDir.exists())
						FileUtils.deleteDir(artifactsStorageDir);
					return null;
				}));
			}
		} else if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
			Long projectId = project.getId();
			var activeServer = projectManager.getActiveServer(projectId, false);
			if (activeServer != null) {
				transactionManager.runAfterCommit(() -> clusterManager.submitToServer(activeServer, () -> {
					File lfsStorageDir = getLfsStorageDir(projectId);
					if (lfsStorageDir != null && lfsStorageDir.exists())
						FileUtils.deleteDir(lfsStorageDir);
					File artifactsStorageDir = getArtifactsStorageDir(projectId, null);
					if (artifactsStorageDir != null && artifactsStorageDir.exists())
						FileUtils.deleteDir(artifactsStorageDir);
					return null;
				}));
			}
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(StorageManager.class);
	}
	
}
