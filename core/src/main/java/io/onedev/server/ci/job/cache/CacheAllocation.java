package io.onedev.server.ci.job.cache;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import io.onedev.server.exception.OneException;

public class CacheAllocation {

	private final File instance; 
	
	private final String path;
	
	public CacheAllocation(File instance, String path) {
		this.instance = instance;
		this.path = path;
	}

	public File getInstance() {
		return instance;
	}

	public String getPath() {
		return path;
	}
	
	public void release() {
		File lockFile = new File(instance, JobCache.LOCK_FILE); 
		if (!lockFile.delete())
			throw new OneException("Unable to delete file: " + lockFile.getAbsolutePath());
	}
	
	public String resolvePath(String basePath) {
		String path = getPath();
		if (path == null)
			path = "";
		if (FilenameUtils.getPrefixLength(path) != 0)
			return path;
		else
			return basePath + "/" + path;
	}
	
	public boolean isWorkspace() {
		return path == null || FilenameUtils.normalize(path).length() == 0;
	}
	
}
