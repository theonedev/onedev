package com.gitplex.server.core.manager;

import java.io.File;

import com.gitplex.server.core.entity.Depot;

public interface AttachmentManager {

	/**
	 * Get directory to store attachment of specified depot and uuid
	 * 
	 * @return
	 * 			directory to store attachment of specified depot and uuid. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentDir(Depot depot, String attachmentDirUUID);

}
