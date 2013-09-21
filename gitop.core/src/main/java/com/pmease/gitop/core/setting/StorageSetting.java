package com.pmease.gitop.core.setting;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class StorageSetting implements Serializable {
	
	private String repoStorageDir;

	@Editable
	@NotEmpty
	public String getRepoStorageDir() {
		return repoStorageDir;
	}

	public void setRepoStorageDir(String repoStorageDir) {
		this.repoStorageDir = repoStorageDir;
	}
	
}
