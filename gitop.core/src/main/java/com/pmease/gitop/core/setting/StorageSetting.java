package com.pmease.gitop.core.setting;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;

@SuppressWarnings("serial")
@Editable
public class StorageSetting implements Serializable {
	
	private String storageDir;

	@Editable
	@NotEmpty
	public String getStorageDir() {
		return storageDir;
	}

	public void setStorageDir(String storageDir) {
		this.storageDir = storageDir;
	}
	
}
