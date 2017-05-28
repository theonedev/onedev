package com.gitplex.server.manager;

import java.io.File;

import com.gitplex.server.model.Project;

public interface StorageManager {
    
	/**
	 * Get directory to store git repository of specified project
	 * 
	 * @return
	 * 			directory to store git repository, the directory will be exist after calling this method
	 */
    File getGitDir(Project project);
    
    /**
     * Get directory to store Lucene index of specified project
     * 
     * @return
     * 			directory to store lucene index, the directory will be exist after calling this method
     */
    File getIndexDir(Project project);

    /**
     * Get directory to store additional info of specified project
     * 
     * @return
     * 			directory to store additional info, the directory will be exist after calling this method
     */
    File getInfoDir(Project project);
    
    /**
     * Get directory to store attachments of specified project
     * 
     * @return 
     * 			directory store attachments, the directory will be exist after calling this method
     */
    File getAttachmentDir(Project project);
    
}
