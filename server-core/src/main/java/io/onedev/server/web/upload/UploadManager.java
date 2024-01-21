package io.onedev.server.web.upload;

import javax.annotation.Nullable;

public interface UploadManager {

	void cacheUpload(FileUpload upload);
	
	void clearUpload(String uploadId);
	
	@Nullable
	FileUpload getUpload(String uploadId);
	
}
