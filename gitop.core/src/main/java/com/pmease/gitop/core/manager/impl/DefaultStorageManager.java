package com.pmease.gitop.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.storage.StorageManager;

@Singleton
public class DefaultStorageManager implements StorageManager {

    private final ConfigManager configManager;

    @Inject
    public DefaultStorageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public File getStorage(Repository repository) {
        return new File(configManager.getSystemSetting().getRepoPath(), repository.getId().toString());
    }

}
