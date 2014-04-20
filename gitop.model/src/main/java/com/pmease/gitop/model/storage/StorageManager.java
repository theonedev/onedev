package com.pmease.gitop.model.storage;

import java.io.File;

import com.pmease.gitop.model.Repository;

public interface StorageManager {
    
    File getStorage(Repository repository);
    
}
