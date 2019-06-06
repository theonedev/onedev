package io.onedev.server.storage;

import java.io.File;

import io.onedev.server.util.facade.ProjectFacade;

public interface AttachmentStorageManager {

	/**
	 * Get directory to store attachment of specified project and uuid
	 * 
	 * @return
	 * 			directory to store attachment of specified project and uuid. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentStorage(ProjectFacade project, String attachmentStorageUUID);

}
