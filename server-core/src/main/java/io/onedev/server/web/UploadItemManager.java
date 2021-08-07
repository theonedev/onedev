package io.onedev.server.web;

import java.util.List;

import org.apache.commons.fileupload.FileItem;

public interface UploadItemManager {

	void setUploadItems(String uploadId, List<FileItem> uploadItems);
	
	List<FileItem> getUploadItems(String uploadId);
	
}
