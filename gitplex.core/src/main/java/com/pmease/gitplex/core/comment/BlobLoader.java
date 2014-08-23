package com.pmease.gitplex.core.comment;

import java.util.List;

import javax.annotation.Nullable;

import com.pmease.commons.git.BlobInfo;

public interface BlobLoader {
	
	/**
	 * Load content of specified blob and convert to lines.
	 * 
	 * @param blobInfo
	 * 			blob info to load
	 * @return
	 * 			lines of the blob, or null if the blob content can not be
	 * 			converted to text
	 */
	@Nullable 
	List<String> loadBlob(BlobInfo blobInfo);
}
