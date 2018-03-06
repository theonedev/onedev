package io.onedev.server.manager;

import java.io.File;

import io.onedev.server.util.facade.ProjectFacade;

public interface AttachmentManager {

	/**
	 * Get directory to store attachment of specified project and uuid
	 * 
	 * @return
	 * 			directory to store attachment of specified project and uuid. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentDir(ProjectFacade project, String attachmentDirUUID);

}
