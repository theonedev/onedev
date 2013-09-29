package com.pmease.gitop.core.storage;

import java.io.File;

import com.pmease.commons.util.FileUtils;

public class RepositoryStorage {
	
	private final File storageDir;
	
	public RepositoryStorage(File storageDir) {
		this.storageDir = storageDir;
	}
	
	public File ofCode() {
		return new File(storageDir, "code");
	}
	
	public File ofWiki() {
		return new File(storageDir, "wiki");
	}
	
	public void delete() {
		FileUtils.deleteDir(storageDir);
	}
	
	public void clean() {
		FileUtils.cleanDir(storageDir);
	}
}
