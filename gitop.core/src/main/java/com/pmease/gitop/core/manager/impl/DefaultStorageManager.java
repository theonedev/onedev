package com.pmease.gitop.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitop.core.manager.ConfigManager;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.storage.ProjectStorage;

@Singleton
public class DefaultStorageManager implements StorageManager {

    private final ConfigManager configManager;

    @Inject
    public DefaultStorageManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public ProjectStorage getStorage(Project project) {
        return new ProjectStorage(new File(configManager.getStorageSetting().getStorageDir(),
                "projects/" + project.getId().toString()));
    }

    @Override
    public File getStorage(User user) {
        return new File(configManager.getStorageSetting().getStorageDir(),
                "users/" + user.getId().toString());
    }

}
