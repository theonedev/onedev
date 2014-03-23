package com.pmease.gitop.model.storage;

import java.io.File;

import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;

public interface StorageManager {
    
    File getStorage(Repository repository);
    
    File getStorage(User user);
    
}
