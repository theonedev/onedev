package io.onedev.server.attachment;

import io.onedev.server.util.artifact.FileInfo;

import java.io.File;
import java.io.InputStream;
import java.util.List;

public interface AttachmentManager {

	File getAttachmentGroupDirLocal(Long projectId, String attachmentGroup);
	
	String saveAttachment(Long projectId, String attachmentGroup, String suggestedAttachmentName, 
			InputStream attachmentStream);
	
	String saveAttachmentLocal(Long projectId, String attachmentGroup, String suggestedAttachmentName, 
			InputStream attachmentStream);
	
	FileInfo getAttachmentInfo(Long projectId, String attachmentGroup, String attachment);
	
	void deleteAttachment(Long projectId, String attachmentGroup, String attachment);
	
	List<FileInfo> listAttachments(Long projectId, String attachmentGroup);
	
}
