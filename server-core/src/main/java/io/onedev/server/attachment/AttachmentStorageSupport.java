package io.onedev.server.attachment;

import io.onedev.server.model.Project;

public interface AttachmentStorageSupport {
	
	Project getAttachmentProject();
	
	String getAttachmentGroup();
	
}
