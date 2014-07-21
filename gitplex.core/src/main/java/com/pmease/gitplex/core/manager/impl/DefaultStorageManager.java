package com.pmease.gitplex.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.manager.ConfigManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.model.Repository;

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
