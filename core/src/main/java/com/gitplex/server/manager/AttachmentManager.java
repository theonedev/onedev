package com.gitplex.server.manager;

import java.io.File;

import com.gitplex.server.model.Project;

public interface AttachmentManager {

	/**
	 * Get directory to store attachment of specified project and uuid
	 * 
	 * @return
	 * 			directory to store attachment of specified project and uuid. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentDir(Project project, String attachmentDirUUID);

}
