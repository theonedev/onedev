package com.pmease.gitplex.web.component.diff;

import java.io.Serializable;

import com.pmease.commons.git.DiffTreeNode;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.git.Git;

@SuppressWarnings("serial")
public class BlobDiffInfo implements Serializable {
	
	public enum Status {ADD, MODIFY, DELETE, RENAME, UNCHANGE}
	
	private final Status status;
	
	private final String oldPath;
	
	private final String newPath;
	
	private final int oldMode;
	
	private final int newMode;
	
	private final String oldRevision;
	
	private final String newRevision;
	
	public BlobDiffInfo(Status status, String oldPath, String newPath, int oldMode, int newMode, 
			String oldRevision, String newRevision) {
		this.status = status;
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.oldMode = oldMode;
		this.newMode = newMode;
		this.oldRevision = oldRevision;
		this.newRevision = newRevision;
	}

	public Status getStatus() {
		return status;
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

	public String getOldRevision() {
		return oldRevision;
	}

	public String getNewRevision() {
		return newRevision;
	}

	public static BlobDiffInfo from(Git git, DiffTreeNode node, String oldRevision, String newRevision) {
		Status status;
		if (node.getStatus() == DiffTreeNode.Status.ADD) 
			status = Status.ADD;
		else if (node.getStatus() == DiffTreeNode.Status.DELETE) 
			status = Status.DELETE;
		else if (node.getStatus() == DiffTreeNode.Status.UNCHANGE) 
			status = Status.UNCHANGE;
		else 
			status = Status.MODIFY;
		return new BlobDiffInfo(status, node.getPath(), node.getPath(), node.getOldMode(), 
				node.getNewMode(), oldRevision, newRevision);
	}
	
	public static BlobDiffInfo from(Git git, FileChange change, String oldRevision, String newRevision) {
		Status status;
		if (change.getStatus() == FileChange.Status.ADD)
			status = Status.ADD;
		else if (change.getStatus() == FileChange.Status.DELETE)
			status = Status.DELETE;
		else if (change.getStatus() == FileChange.Status.MODIFY)
			status = Status.MODIFY;
		else 
			status = Status.RENAME;
		
		return new BlobDiffInfo(status, change.getOldPath(), change.getNewPath(), 
				change.getOldMode(), change.getNewMode(), oldRevision, newRevision);
	}
}
