package io.onedev.server.ee.storage;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.StorageManager;
import io.onedev.server.SubscriptionManager;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Files;

import static io.onedev.commons.bootstrap.Bootstrap.getSiteDir;
import static io.onedev.server.model.Build.getArtifactsDir;

@Singleton
public class DefaultStorageManager implements StorageManager, Serializable {
	
	private final SettingManager settingManager;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private final ProjectManager projectManager;
	
	private final SubscriptionManager subscriptionManager;
	
	@Inject
	public DefaultStorageManager(SettingManager settingManager, TransactionManager transactionManager,
								 ClusterManager clusterManager, ProjectManager projectManager,
								 SubscriptionManager subscriptionManager) { 
		this.settingManager = settingManager;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
		this.projectManager = projectManager;
		this.subscriptionManager = subscriptionManager;
	}
	
	@Nullable
	private File getLfsStorageDir(Long projectId) {
		StorageSetting storageSetting = settingManager.getContributedSetting(StorageSetting.class);
		if (storageSetting != null && storageSetting.getLfsStore() != null) {
			var lfsStoreDir = getSiteDir().toPath().resolve(storageSetting.getLfsStore()).toFile();
			if (!lfsStoreDir.exists())
				throw new ExplicitException("Lfs store directory not exist");
			return new File(lfsStoreDir, String.valueOf(projectId));
		} else {
			return null;
		}
	}

	@Nullable
	private File getArtifactsStorageDir(Long projectId, @Nullable Long buildNumber) {
		StorageSetting storageSetting = settingManager.getContributedSetting(StorageSetting.class);
		if (storageSetting != null && storageSetting.getArtifactStore() != null) {
			var artifactsStoreDir = getSiteDir().toPath().resolve(storageSetting.getArtifactStore()).toFile();
			if (!artifactsStoreDir.exists())
				throw new ExplicitException("Artifacts store directory not exist");
			String subpath = String.valueOf(projectId);
			if (buildNumber != null)
				subpath += "/" + Build.getStoragePath(buildNumber);
			return new File(artifactsStoreDir, subpath);
		} else {
			return null;
		}
	}

	@Override
	public File initLfsDir(Long projectId) {
		if (subscriptionManager.isSubscriptionActive()) {
			File gitDir = projectManager.getGitDir(projectId);
			File lfsStorageDir = getLfsStorageDir(projectId);
			var lfsDir = new File(gitDir, "lfs");
			if (lfsStorageDir != null) {
				if (!lfsDir.exists()) {
					FileUtils.createDir(lfsStorageDir);
					try {
						Files.createSymbolicLink(lfsDir.toPath(), lfsStorageDir.toPath());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				FileUtils.createDir(lfsDir);
			}
			return lfsDir;
		} else {
			var lfsDir = new File(projectManager.getGitDir(projectId), "lfs");
			FileUtils.createDir(lfsDir);
			return lfsDir;
		}
	}

	@Override
	public File initArtifactsDir(Long projectId, Long buildNumber) {
		if (subscriptionManager.isSubscriptionActive()) {
			File artifactsStorageDir = getArtifactsStorageDir(projectId, buildNumber);
			File artifactsDir = getArtifactsDir(projectId, buildNumber);
			if (artifactsStorageDir != null) {
				if (!artifactsDir.exists()) {
					FileUtils.createDir(artifactsStorageDir);
					try {
						Files.createSymbolicLink(
								artifactsDir.toPath(),
								artifactsStorageDir.toPath());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			} else {
				FileUtils.createDir(artifactsDir);
			}
			return artifactsDir;
		} else {
			var artifactsDir = getArtifactsDir(projectId, buildNumber);
			FileUtils.createDir(artifactsDir);
			return artifactsDir;
		}
	}
	
	@Listen
	public void on(EntityRemoved event) {
		if (subscriptionManager.isSubscriptionActive()) {
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
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(StorageManager.class);
	}
	
}
