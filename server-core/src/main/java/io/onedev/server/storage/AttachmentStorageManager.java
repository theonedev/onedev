package io.onedev.server.storage;

import java.io.File;

import io.onedev.server.model.Project;

public interface AttachmentStorageManager {

	/**
	 * Get directory to store attachment of specified project and group
	 * 
	 * @return
	 * 			directory to store attachment of specified project and group. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentGroupDir(Project project, String attachmentGroup);

}
