package com.pmease.commons.git;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public class ParentPathAndName implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final String parentPath;
	
	private final String name;

	public ParentPathAndName(String path) {
		if (path.contains("/")) {
			parentPath = StringUtils.substringBeforeLast(path, "/");
			name = StringUtils.substringAfterLast(path, "/");
		} else {
			parentPath = null;
			name = path;
		}
	}
	
	public ParentPathAndName(@Nullable String parentPath, @Nullable String name) {
		this.parentPath = parentPath;
		this.name = name;
	}

	@Nullable
	public String getParentPath() {
		return parentPath;
	}

	@Nullable
	public String getName() {
		return name;
	}
	
	public String getPath() {
		if (name == null)
			return null;
		else if (parentPath != null)
			return GitUtils.normalizePath(parentPath + "/" + name);
		else
			return GitUtils.normalizePath(name);
	}
	
	public String getPath(String name) {
		if (parentPath != null)
			return GitUtils.normalizePath(parentPath + "/" + name);
		else
			return GitUtils.normalizePath(name);
	}
}
