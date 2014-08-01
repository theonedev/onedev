package com.pmease.commons.git;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("serial")
public class DiffTreeNode implements Comparable<DiffTreeNode>, Serializable {

	public enum Action {ADD, MODIFY, DELETE, EQUAL};

	private final String path;
	
	private final boolean folder;
	
	private final Action action;
	
	private transient String name;

	public DiffTreeNode(String path, boolean folder, Action action) {
		this.path = path;
		this.folder = folder;
		this.action = action;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		if (name == null) {
			if (path.contains("/"))
				name = StringUtils.substringAfterLast(path, "/");
			else
				name = path;
		}
		return name;
	}
	
	public Action getAction() {
		return action;
	}

	public boolean isFolder() {
		return folder;
	}

	@Override
	public int compareTo(DiffTreeNode other) {
		if (isFolder()) {
			if (other.isFolder())
				return getPath().compareTo(other.getPath());
			else
				return -1;
		} else if (other.isFolder()) {
			return 1;
		} else {
			return getPath().compareTo(other.getPath());
		}
	}

}
