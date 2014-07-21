package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.gitplex.core.model.Repository;

public interface StorageManager {
    
    File getStorage(Repository repository);
    
}
