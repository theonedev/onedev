package com.pmease.commons.git;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Objects;

@SuppressWarnings("serial")
public class DiffTreeNode implements Comparable<DiffTreeNode>, Serializable {

	public enum Status {ADD, MODIFY, DELETE, UNCHANGE};

	private final Status status;
	
	private final String path;
	
	private final int oldMode;
	
	private final int newMode;
	
	private transient String name;

	public DiffTreeNode(Status status, String path, int oldMode, int newMode) {
		this.path = path;
		this.status = status;
		this.oldMode = oldMode;
		this.newMode = newMode;
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
	
	public Status getStatus() {
		return status;
	}

	public int getOldMode() {
		return oldMode;
	}

	public int getNewMode() {
		return newMode;
	}
	
	public boolean isFolder() {
		return oldMode == FileMode.TYPE_TREE || newMode == FileMode.TYPE_TREE;
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

	@Override
	public String toString() {
		return Objects.toStringHelper(DiffTreeNode.class)
				.add("action", status)
				.add("path", path)
				.toString();
	}

}
