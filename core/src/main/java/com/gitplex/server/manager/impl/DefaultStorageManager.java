package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.event.depot.DepotDeleted;
import com.gitplex.server.event.lifecycle.SystemStarting;
import com.gitplex.server.manager.ConfigManager;
import com.gitplex.server.manager.StorageManager;
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
    
    private File getDepotsDir() {
    	File depotsDir = new File(getStorageDir(), "repositories");
    	FileUtils.createDir(depotsDir);
    	return depotsDir;
    }
    
    private File getDepotDir(Depot depot) {
        File depotDir = new File(getDepotsDir(), String.valueOf(depot.getId()));
        FileUtils.createDir(depotDir);
        return depotDir;
    }
    
    @Override
    public File getGitDir(Depot depot) {
        File gitDir = new File(getDepotDir(depot), "git");
        FileUtils.createDir(gitDir);
        return gitDir;
    }

	@Override
	public File getInfoDir(Depot depot) {
        File cacheDir = new File(getDepotDir(depot), "info");
        FileUtils.createDir(cacheDir);
        return cacheDir;
	}

	@Override
	public File getIndexDir(Depot depot) {
        File indexDir = new File(getDepotDir(depot), "index");
        FileUtils.createDir(indexDir);
        return indexDir;
	}

	@Override
	public File getAttachmentDir(Depot depot) {
        File attachmentDir = new File(getDepotDir(depot), "attachment");
        FileUtils.createDir(attachmentDir);
        return attachmentDir;
	}

	@Listen
	public void on(SystemStarting event) {
        for (File depotDir: getDepotsDir().listFiles()) {
        	if (new File(depotDir, DELETE_MARK).exists()) { 
        		logger.info("Deleting directory marked for deletion: " + depotDir);
        		FileUtils.deleteDir(depotDir);
        	}
        }
	}

	@Transactional
	@Listen
	public void on(DepotDeleted event) {
		dao.doAfterCommit(new Runnable() {

			@Override
			public void run() {
				try {
					new File(getDepotDir(event.getDepot()), DELETE_MARK).createNewFile();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}
	
}
