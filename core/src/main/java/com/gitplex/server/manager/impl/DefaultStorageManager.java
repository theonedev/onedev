package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStarting;
import com.gitplex.server.event.project.ProjectDeleted;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.util.FileUtils;

@Singleton
public class DefaultStorageManager implements StorageManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultStorageManager.class);
	
	private static final String DELETE_MARK = "to_be_deleted_when_gitplex_is_restarted";
	
	private final Dao dao;
	
    private final ConfigManager configManager;
    
    @Inject
    public DefaultStorageManager(Dao dao, ConfigManager configManager) {
    	this.dao = dao;
        this.configManager = configManager;
    }

    private File getStorageDir() {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
    	FileUtils.createDir(storageDir);
    	return storageDir;
    }
    
    private File getProjectsDir() {
    	File projectsDir = new File(getStorageDir(), "projects");
    	FileUtils.createDir(projectsDir);
    	return projectsDir;
    }
    
    private File getProjectDir(Project project) {
        File projectDir = new File(getProjectsDir(), String.valueOf(project.getId()));
        FileUtils.createDir(projectDir);
        return projectDir;
    }
    
    @Override
    public File getGitDir(Project project) {
        File gitDir = new File(getProjectDir(project), "git");
        FileUtils.createDir(gitDir);
        return gitDir;
    }

	@Override
	public File getInfoDir(Project project) {
        File cacheDir = new File(getProjectDir(project), "info");
        FileUtils.createDir(cacheDir);
        return cacheDir;
	}

	@Override
	public File getIndexDir(Project project) {
        File indexDir = new File(getProjectDir(project), "index");
        FileUtils.createDir(indexDir);
        return indexDir;
	}

	@Override
	public File getAttachmentDir(Project project) {
        File attachmentDir = new File(getProjectDir(project), "attachment");
        FileUtils.createDir(attachmentDir);
        return attachmentDir;
	}

	@Listen
	public void on(SystemStarting event) {
        for (File projectDir: getProjectsDir().listFiles()) {
        	if (new File(projectDir, DELETE_MARK).exists()) { 
        		logger.info("Deleting directory marked for deletion: " + projectDir);
        		FileUtils.deleteDir(projectDir);
        	}
        }
	}

	@Transactional
	@Listen
	public void on(ProjectDeleted event) {
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				try {
					new File(getProjectDir(event.getProject()), DELETE_MARK).createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
}
