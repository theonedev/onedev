package com.pmease.gitplex.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.util.FileUtils;
import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultStorageManager implements StorageManager {

    private final ConfigManager configManager;

    @Inject
    public DefaultStorageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public File getRepoDir(Repository repository) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        return createIfNotExist(new File(storageDir, "repositories/" + repository.getId()));
    }

	@Override
	public File getCacheDir(Repository repository) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        return createIfNotExist(new File(storageDir, "caches/repositories/" + repository.getId()));
	}

	@Override
	public File getCacheDir(PullRequest request) {
		File repoCacheDir = getCacheDir(request.getTarget().getRepository());
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
	public File getIndexDir(Repository repository) {
    	File storageDir = new File(configManager.getSystemSetting().getStoragePath());
        return createIfNotExist(new File(storageDir, "indexes/repositories/" + repository.getId()));
	}

}
