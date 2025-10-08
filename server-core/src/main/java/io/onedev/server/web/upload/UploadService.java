package io.onedev.server.web.upload;

import org.jspecify.annotations.Nullable;

public interface UploadService {

	void cacheUpload(FileUpload upload);
	
	void clearUpload(String uploadId);
	
	@Nullable
	FileUpload getUpload(String uploadId);
	
}
