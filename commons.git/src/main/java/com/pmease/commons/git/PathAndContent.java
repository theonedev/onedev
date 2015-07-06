package com.pmease.commons.git;

import java.io.Serializable;

import com.google.common.base.Preconditions;

public class PathAndContent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String path;
	
	private final byte[] content;

	public PathAndContent(String path, byte[] content) {
		this.path = Preconditions.checkNotNull(GitUtils.normalizePath(path));
		this.content = content;
	}

	public String getPath() {
		return path;
	}

	public byte[] getContent() {
		return content;
	}
	
}
