package io.onedev.server.storage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterRunnable;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
public class DefaultStorageManager implements StorageManager, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultStorageManager.class);
	
	private static final String OLD_DELETE_MARK1 = "to_be_deleted_when_gitplex_is_restarted";
	
	private static final String OLD_DELETE_MARK2 = "to_be_deleted_when_turbodev_is_restarted";
	
	private static final String DELETE_MARK = "to-be-deleted-when-onedev-is-restarted";

	protected final ProjectManager projectManager;
	
	protected final TransactionManager transactionManager;
	
	protected final ClusterManager clusterManager;
	
    @Inject
    public DefaultStorageManager(ProjectManager projectManager, TransactionManager transactionManager, 
    		ClusterManager clusterManager) {
    	this.projectManager = projectManager;
        this.transactionManager = transactionManager;
        this.clusterManager = clusterManager;
    }

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(StorageManager.class);
	}
	
    @Override
    public File getProjectsDir() {
    	File projectsDir = new File(Bootstrap.getSiteDir(), "projects");
    	FileUtils.createDir(projectsDir);
    	return projectsDir;
    }
    
    @Override
    public File getProjectDir(Long projectId) {
        return new File(getProjectsDir(), String.valueOf(projectId));
    }
    
    private File getProjectSubdir(Long projectId, String subdirName) {
    	File projectDir = getProjectDir(projectId);
    	if (projectDir.exists()) {
    		File subDir = new File(projectDir, subdirName);
    		FileUtils.createDir(subDir);
    		return subDir;
    	} else {
    		throw new ExplicitException("Storage directory not found for project id " + projectId);
    	}
    }
    
    @Override
    public File getProjectGitDir(Long projectId) {
        return getProjectSubdir(projectId, "git");
    }

	@Override
	public File getProjectInfoDir(Long projectId) {
        return getProjectSubdir(projectId, "info");
	}

	@Override
	public File getProjectIndexDir(Long projectId) {
        return getProjectSubdir(projectId, "index");
	}

	@Override
	public File getProjectSiteDir(Long projectId) {
        return getProjectSubdir(projectId, "site");
	}
	
	@Override
	public File getProjectAttachmentDir(Long projectId) {
        return getProjectSubdir(projectId, "attachment");
	}
	
	private File getBuildsDir(Long projectId) {
        return getProjectSubdir(projectId, "builds");
	}
	
	@Override
	public File getBuildDir(Long projectId, Long buildNumber) {
		File buildsDir = getBuildsDir(projectId);
		File buildDir = new File(buildsDir, String.valueOf(buildNumber));
		if (buildsDir.exists())
			FileUtils.createDir(buildDir);
		return buildDir;
	}

	@Override
	public void initLfsDir(Long projectId) {
		FileUtils.createDir(new File(getProjectGitDir(projectId), "lfs"));
	}

	@Override
	public void initArtifactsDir(Long projectId, Long buildNumber) {
		File buildDir = getBuildDir(projectId, buildNumber);
		FileUtils.createDir(new File(buildDir, Build.ARTIFACTS_DIR));
	}

	@Override
	public File getIndexDir() {
    	File indexDir = new File(Bootstrap.getSiteDir(), "index");
    	FileUtils.createDir(indexDir);
    	return indexDir;
	}
	
	@Listen
	public void on(SystemStarting event) {
        for (File projectDir: getProjectsDir().listFiles()) {
        	if (new File(projectDir, OLD_DELETE_MARK1).exists()
        			|| new File(projectDir, OLD_DELETE_MARK2).exists()
        			|| new File(projectDir, DELETE_MARK).exists()) { 
        		logger.info("Deleting directory marked for deletion: " + projectDir);
        		FileUtils.deleteDir(projectDir);
        	}
        }
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		Long id = event.getEntity().getId();
		if (event.getEntity() instanceof Project) {
			UUID storageServerUUID = projectManager.getStorageServerUUID(id, false);
			if (storageServerUUID != null) {
				transactionManager.runAfterCommit(new ClusterRunnable() {

					private static final long serialVersionUID = 1L;

					@Override
					public void run() {
						clusterManager.submitToServer(storageServerUUID, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
								File projectDir = getProjectDir(id);
								if (projectDir.exists()) {
									try {
										new File(projectDir, DELETE_MARK).createNewFile();
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}
								return null;
							}
							
						});
					}
					
				});
			}
		} else if (event.getEntity() instanceof Build) {
			Build build = (Build) event.getEntity();
	    	Long projectId = build.getProject().getId();
	    	Long buildNumber = build.getNumber();

	    	UUID storageServerUUID = projectManager.getStorageServerUUID(projectId, false);
	    	
			transactionManager.runAfterCommit(new ClusterRunnable() {

				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					if (storageServerUUID != null) {
				    	clusterManager.submitToServer(storageServerUUID, new ClusterTask<Void>() {

							private static final long serialVersionUID = 1L;

							@Override
							public Void call() throws Exception {
						    	FileUtils.deleteDir(getBuildDir(projectId, buildNumber));
								return null;
							}
				    		
				    	});
						
					}
				}
				
			});
		}
	}
	
}
