package com.pmease.gitop.model.storage;

import java.io.File;

import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;

public interface StorageManager {
    
    ProjectStorage getStorage(Project project);
    
    File getStorage(User user);
    
}
