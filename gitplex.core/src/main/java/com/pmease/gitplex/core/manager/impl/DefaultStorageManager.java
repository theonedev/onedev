package com.pmease.gitplex.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.StorageManager;

@Singleton
public class DefaultStorageManager implements StorageManager {

    private final ConfigManager configManager;

    @Inject
    public DefaultStorageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public File getDepotDir(Depot depot) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        return createIfNotExist(new File(storageDir, "repositories/" + depot.getId()));
    }

	@Override
	public File getCacheDir(Depot depot) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        return createIfNotExist(new File(storageDir, "caches/repositories/" + depot.getId()));
	}

	@Override
	public File getCacheDir(PullRequest request) {
		File repoCacheDir = getCacheDir(request.getTargetDepot());
        return createIfNotExist(new File(repoCacheDir, "requests/" + request.getId()));
	}

	@Override
	public File getCacheDir(PullRequestUpdate update) {
		File requestCacheDir = getCacheDir(update.getRequest());
        return createIfNotExist(new File(requestCacheDir, "updates/" + update.getId()));
	}

	private File createIfNotExist(File dir) {
		if (!dir.exists()) 
			FileUtils.createDir(dir);
		return dir;
	}

	@Override
	public File getIndexDir(Depot depot) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        return createIfNotExist(new File(storageDir, "indexes/repositories/" + depot.getId()));
	}

	@Override
	public File getAttachmentsDir(Depot depot) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
    	File attachmentsDir = new File(storageDir, "attachments/repositories/" + depot.getId());
        return createIfNotExist(attachmentsDir);
	}

}
