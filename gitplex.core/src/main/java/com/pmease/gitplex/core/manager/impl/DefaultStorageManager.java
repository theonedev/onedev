package com.pmease.gitplex.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.StorageManager;

@Singleton
public class DefaultStorageManager implements StorageManager {

    private final ConfigManager configManager;
    
    @Inject
    public DefaultStorageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private File getDepotDir(Depot depot) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        File depotDir = new File(storageDir, "repositories/" + depot.getId());
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

}
