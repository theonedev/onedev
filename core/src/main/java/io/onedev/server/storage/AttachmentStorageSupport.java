package io.onedev.server.storage;

import io.onedev.server.model.Project;

public interface AttachmentStorageSupport {
	
	Project getAttachmentProject();
	
	String getAttachmentStorageUUID();
	
}
