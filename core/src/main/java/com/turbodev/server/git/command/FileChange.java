package com.turbodev.server.git.command;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FileChange implements Serializable {
	
	public enum Action {ADD, MODIFY, DELETE, COPY, RENAME, TYPE}
	
	private final String oldPath;
	
	private final String newPath;
	
	private final Action action;
	
	private final int additions;
	
	private final int deletions;
	
	public FileChange(Action action, String oldPath, String newPath, int additions, int deletions) {
		this.action = action;
		
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.additions = additions;
		this.deletions = deletions;
	}

	public String getOldPath() {
		return oldPath;
	}
	
	public String getNewPath() {
		return newPath;
	}

	public Action getAction() {
		return action;
	}

	public int getAdditions() {
		return additions;
	}

	public int getDeletions() {
		return deletions;
	}

	@Override
	public String toString() {
		if (action == Action.COPY || action == Action.RENAME)
			return action.name() + "\t" + oldPath + "->" + newPath;
		else 
			return action.name() + "\t" + newPath;
	}
	
}