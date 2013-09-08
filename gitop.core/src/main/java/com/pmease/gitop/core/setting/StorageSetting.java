package com.pmease.gitop.core.setting;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StorageSetting implements Serializable {
	
	private String repoStorageDir;

	public String getRepoStorageDir() {
		return repoStorageDir;
	}

	public void setRepoStorageDir(String repoStorageDir) {
		this.repoStorageDir = repoStorageDir;
	}
	
}
