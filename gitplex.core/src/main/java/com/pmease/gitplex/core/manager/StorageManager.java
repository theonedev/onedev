package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;

public interface StorageManager {
    
	/**
	 * Get directory to store git repository of specified depot
	 * 
	 * @return
	 * 			directory to store git repository, the directory will be exist after calling this method
	 */
    File getGitDir(Depot depot);
    
    /**
     * Get directory to store Lucene index of specified depot
     * 
     * @return
     * 			directory to store lucene index, the directory will be exist after calling this method
     */
    File getIndexDir(Depot depot);

    /**
     * Get directory to store additional info of specified depot
     * 
     * @return
     * 			directory to store additional info, the directory will be exist after calling this method
     */
    File getInfoDir(Depot depot);
    
    /**
     * Get directory to store cache data of specified pull request
     * 
     * @return
     * 			directory to store cache data, the directory will be exist after calling this method
     */
    File getCacheDir(PullRequest request);
    
    /**
     * Get directory to store cache data of specified pull request update
     * 
     * @return
     * 			directory to store cache data, the directory will be exist after calling this method
     */
    File getCacheDir(PullRequestUpdate update);

    /**
     * Get directory to store attachments of specified depot
     * 
     * @return 
     * 			directory store attachments, the directory will be exist after calling this method
     */
    File getAttachmentDir(Depot depot);
    
}
