package com.gitplex.core.manager;

import java.io.File;

import com.gitplex.core.entity.Depot;

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
     * Get directory to store attachments of specified depot
     * 
     * @return 
     * 			directory store attachments, the directory will be exist after calling this method
     */
    File getAttachmentDir(Depot depot);
    
}
