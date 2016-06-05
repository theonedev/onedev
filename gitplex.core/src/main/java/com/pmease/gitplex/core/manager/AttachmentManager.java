package com.pmease.gitplex.core.manager;

import java.io.File;

import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.listener.CodeCommentListener;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.PullRequestListener;

public interface AttachmentManager extends DepotListener, CodeCommentListener, LifecycleListener, 
		PullRequestListener {

	/**
	 * Get directory to store attachment of specified depot and uuid
	 * 
	 * @return
	 * 			directory to store attachment of specified depot and uuid. The directory may not exist 
	 * 			if there is no any attachment saved
	 */
    File getAttachmentDir(Depot depot, String attachmentDirUUID);

}
