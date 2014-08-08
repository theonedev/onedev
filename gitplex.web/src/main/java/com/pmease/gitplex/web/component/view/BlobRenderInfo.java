package com.pmease.gitplex.web.component.view;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BlobRenderInfo implements Serializable {

	private final String path;
	
	private final String revision;
	
	private final int mode;

	public BlobRenderInfo(String path, String revision, int mode) {
		this.path = path;
		this.revision = revision;
		this.mode = mode;
	}
	
	public String getPath() {
		return path;
	}

	public String getRevision() {
		return revision;
	}

	public int getMode() {
		return mode;
	}
	
}
