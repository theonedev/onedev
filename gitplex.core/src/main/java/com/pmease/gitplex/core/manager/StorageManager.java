package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;

public interface StorageManager {
    
    File getRepoDir(Repository repository);
    
    File getIndexDir(Repository repository);
    
    File getCacheDir(Repository repository);
    
    File getCacheDir(PullRequest request);
    
    File getAttachmentsDir(PullRequest request);
    
    File getCacheDir(PullRequestUpdate update);

}
