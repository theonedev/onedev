package io.onedev.server.attachment;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import io.onedev.server.util.FileInfo;

public interface AttachmentManager {

	File getAttachmentGroupDirLocal(Long projectId, String attachmentGroup);
	
    void moveAttachmentGroupTargetLocal(Long targetProjectId, Long sourceProjectId, String attachmentGroup);
    
	String saveAttachment(Long projectId, String attachmentGroup, String suggestedAttachmentName, 
			InputStream attachmentStream);
	
	String saveAttachmentLocal(Long projectId, String attachmentGroup, String suggestedAttachmentName, 
			InputStream attachmentStream);
	
	FileInfo getAttachmentInfo(Long projectId, String attachmentGroup, String attachment);
	
	void deleteAttachment(Long projectId, String attachmentGroup, String attachment);
	
	List<FileInfo> listAttachments(Long projectId, String attachmentGroup);
	
}
