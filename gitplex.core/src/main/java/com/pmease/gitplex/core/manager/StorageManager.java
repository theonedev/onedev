package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface StorageManager {
    
    File getDepotDir(Depot depot);
    
    File getIndexDir(Depot depot);
    
    File getCacheDir(Depot depot);
    
    File getCacheDir(PullRequest request);
    
    File getAttachmentsDir(PullRequest request);
    
    File getCacheDir(PullRequestUpdate update);

}
