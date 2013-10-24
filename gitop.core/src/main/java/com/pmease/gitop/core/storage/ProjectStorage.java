package com.pmease.gitop.core.storage;

import java.io.File;

import com.pmease.commons.util.FileUtils;

public class ProjectStorage {
	
	private final File storageDir;
	
	public ProjectStorage(File storageDir) {
		this.storageDir = storageDir;
	}
	
	/**
	 * Get directory holding bare repository of code.
	 * 
	 * @return
	 *         directory holding bare repository of code
	 */
	public File ofCode() {
		return new File(storageDir, "code");
	}
	
	/**
	 * Get directory holding working copy of code. This working directory 
	 * is for purpose of merging code.
	 * 
	 * @return
	 *         directory holding working copy of code
	 */
	public File ofWorkingCode() {
	    return new File(storageDir, "workingCode");
	}
	
	public File ofWiki() {
		return new File(storageDir, "wiki");
	}
	
	public File ofWorkingWiki() {
	    return new File(storageDir, "workingWiki");
	}
	
	public void delete() {
		FileUtils.deleteDir(storageDir);
	}
	
	public void clean() {
		FileUtils.cleanDir(storageDir);
	}
}
