package io.onedev.server.attachment;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import io.onedev.server.util.artifact.FileInfo;

public interface AttachmentService {

	File getAttachmentGroupDir(Long projectId, String attachmentGroup);
	
	String saveAttachment(Long projectId, String attachmentGroup, String preferredAttachmentName, 
			InputStream attachmentStream);

	String saveAttachmentLocal(Long projectId, String attachmentGroup, String preferredAttachmentName,
						  InputStream attachmentStream);
	
	FileInfo getAttachmentInfo(Long projectId, String attachmentGroup, String attachment);
	
	void deleteAttachment(Long projectId, String attachmentGroup, String attachment);
	
	List<FileInfo> listAttachments(Long projectId, String attachmentGroup);

	void syncAttachments(Long projectId, String activeServer);
	
	String getAttachmentLockName(Long projectId, String attachmentGroup);
	
}
