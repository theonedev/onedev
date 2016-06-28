package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.commons.hibernate.dao.DaoListener;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.LifecycleListener;

public interface AttachmentManager extends DaoListener, LifecycleListener {

	/**
	 * Get directory to store attachment of specified depot and uuid
	 * 
	 * @return
	 * 			directory to store attachment of specified depot and uuid. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentDir(Depot depot, String attachmentDirUUID);

}
