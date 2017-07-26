package com.gitplex.server.git.command;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FileChange implements Serializable {
	
	public enum Action {ADD, MODIFY, DELETE, COPY, RENAME, TYPE}
	
	private final String oldPath;
	
	private final String newPath;
	
	private final Action action;
	
	public FileChange(Action action, String oldPath, String newPath) {
		this.action = action;
		
		this.oldPath = oldPath;
		this.newPath = newPath;
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

	@Override
	public String toString() {
		if (action == Action.COPY || action == Action.RENAME)
			return action.name() + "\t" + oldPath + "->" + newPath;
		else 
			return action.name() + "\t" + newPath;
	}
	
}