package com.pmease.commons.git;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FileChange implements Serializable {
	public enum Action {ADD, MODIFY, DELETE}
	
	private final String path;
	
	private final Action action;
	
	public FileChange(Action action, String path) {
		this.action = action;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public Action getAction() {
		return action;
	}

	@Override
	public String toString() {
		return action.name() + "    " + path;
	}
	
}
