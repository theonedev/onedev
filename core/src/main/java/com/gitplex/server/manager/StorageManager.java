package com.gitplex.server.manager;

import java.io.File;

public interface StorageManager {
    
	/**
	 * Get directory to store git repository of specified project
	 * 
	 * @return
	 * 			directory to store git repository, the directory will be exist after calling this method
	 */
    File getProjectGitDir(Long projectId);
    
    /**
     * Get directory to store Lucene index of specified project
     * 
     * @return
     * 			directory to store lucene index, the directory will be exist after calling this method
     */
    File getProjectIndexDir(Long projectId);

    /**
     * Get directory to store additional info of specified project
     * 
     * @return
     * 			directory to store additional info, the directory will be exist after calling this method
     */
    File getProjectInfoDir(Long projectId);
    
    /**
     * Get directory to store attachments of specified project
     * 
     * @return 
     * 			directory store attachments, the directory will be exist after calling this method
     */
    File getProjectAttachmentDir(Long projectId);
    
    File getUserInfoDir(Long userId);
    
}
