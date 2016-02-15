package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Depot;

public interface StorageManager {
    
    File getDepotDir(Depot depot);
    
    File getIndexDir(Depot depot);
    
    File getCacheDir(Depot depot);
    
    File getCacheDir(PullRequest request);
    
    File getAttachmentsDir(PullRequest request);
    
    File getCacheDir(PullRequestUpdate update);

}
