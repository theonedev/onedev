package com.gitplex.server.git.command;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FileChange implements Serializable {
	
	private final String oldPath;
	
	private final String path;
	
	private final int additions;
	
	private final int deletions;
	
	public FileChange(String oldPath, String newPath, int additions, int deletions) {
		this.oldPath = oldPath;
		this.path = newPath;
		this.additions = additions;
		this.deletions = deletions;
	}

	public String getOldPath() {
		return oldPath;
	}
	
	public String getPath() {
		return path;
	}

	public int getAdditions() {
		return additions;
	}

	public int getDeletions() {
		return deletions;
	}

	@Override
	public String toString() {
		if (oldPath != null)
			return oldPath + "=>" + path;
		else 
			return path;
	}
	
}