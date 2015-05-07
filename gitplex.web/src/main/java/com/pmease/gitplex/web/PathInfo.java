package com.pmease.gitplex.web;

import java.io.Serializable;

public class PathInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String path;
	
	private final int mode;
	
	public PathInfo(String path, int mode) {
		this.path = path;
		this.mode = mode;
	}

	public String getPath() {
		return path;
	}

	public int getMode() {
		return mode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PathInfo) 
			return path.equals(((PathInfo)obj).getPath());
		else 
			return false;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
}