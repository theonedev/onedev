package io.onedev.server.storage;

import java.io.File;

public interface StorageManager {
    
	/**
	 * Get directory to store git repository of specified project
	 * 
	 * @return
	 * 			directory to store git repository. The directory will be exist after calling this method
	 */
    File getProjectGitDir(Long projectId);
    
    /**
     * Get directory to store Lucene index of specified project
     * 
     * @return
     * 			directory to store lucene index. The directory will be exist after calling this method
     */
    File getProjectIndexDir(Long projectId);

    /**
     * Get directory to store additional info of specified project
     * 
     * @return
     * 			directory to store additional info. The directory will be exist after calling this method
     */
    File getProjectInfoDir(Long projectId);
    
    /**
     * Get directory to store attachments of specified project
     * 
     * @return 
     * 			directory store attachments. The directory will be exist after calling this method
     */
    File getProjectAttachmentDir(Long projectId);
    
    File getUserInfoDir(Long userId);
    
    /**
     * Get directory to store build related files such as logs, artifacts and reports
     * 
     * @return 
     * 			directory store build related files such as logs, artifacts and reports. The directory 
     * 			will be exist after calling this method
     */
    File getBuildDir(Long projectId, Long buildNumber);
    
}
