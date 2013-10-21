package com.pmease.gitop.core.manager;

import java.io.File;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.manager.impl.DefaultStorageManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.storage.ProjectStorage;

@ImplementedBy(DefaultStorageManager.class)
public interface StorageManager {
    
    ProjectStorage getStorage(Project project);
    
    File getStorage(User user);
    
}
