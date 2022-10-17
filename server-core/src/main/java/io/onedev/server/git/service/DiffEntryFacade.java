package io.onedev.server.git.service;

import java.io.Serializable;

import org.eclipse.jgit.diff.DiffEntry;

public class DiffEntryFacade implements Serializable {

	private static final long serialVersionUID = 1L;

	private final DiffEntry.ChangeType changeType;
	
	private final String oldPath; 
	
	private final String newPath;
	
	private final int oldMode;
	
	private final int newMode;
	
	public DiffEntryFacade(DiffEntry.ChangeType changeType, String oldPath, String newPath, 
			int oldMode, int newMode) {
		this.changeType = changeType;
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.oldMode = oldMode;
		this.newMode = newMode;
	}

	public DiffEntry.ChangeType getChangeType() {
		return changeType;
	}

	public String getOldPath() {
		return oldPath;
	}

	public String getNewPath() {
		return newPath;
	}

	public int getOldMode() {
		return oldMode;
	}

	public int getNewMode() {
		return newMode;
	}
	
}
