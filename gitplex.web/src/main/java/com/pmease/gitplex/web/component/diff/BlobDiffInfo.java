package com.pmease.gitplex.web.component.diff;

import com.pmease.commons.git.DiffTreeNode;
import com.pmease.commons.git.FileChange;
import com.pmease.commons.git.Git;

public class BlobDiffInfo {
	
	public enum Status {ADD, MODIFY, DELETE, RENAME, UNCHANGE}
	
	private final Status status;
	
	private final String oldPath;
	
	private final String newPath;
	
	private final int oldMode;
	
	private final int newMode;
	
	private final String oldRevision;
	
	private final String newRevision;
	
	private final byte[] oldContent;
	
	private final byte[] newContent;
	
	public BlobDiffInfo(Status status, String oldPath, String newPath, int oldMode, int newMode, 
			String oldRevision, String newRevision, byte[] oldContent, byte[] newContent) {
		this.status = status;
		this.oldPath = oldPath;
		this.newPath = newPath;
		this.oldMode = oldMode;
		this.newMode = newMode;
		this.oldContent = oldContent;
		this.newContent = newContent;
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

	public byte[] getOldContent() {
		return oldContent;
	}

	public byte[] getNewContent() {
		return newContent;
	}

	public static BlobDiffInfo from(Git git, DiffTreeNode node, String oldRevision, String newRevision) {
		Status status;
		byte[] oldContent, newContent;
		if (node.getStatus() == DiffTreeNode.Status.ADD) {
			status = Status.ADD;
			oldContent = null;
			newContent = git.read(newRevision, node.getPath(), node.getNewMode());
		} else if (node.getStatus() == DiffTreeNode.Status.DELETE) {
			status = Status.DELETE;
			oldContent = git.read(oldRevision, node.getPath(), node.getOldMode());
			newContent = null;
		} else if (node.getStatus() == DiffTreeNode.Status.UNCHANGE) {
			status = Status.UNCHANGE;
			oldContent = newContent = git.read(newRevision, node.getPath(), node.getNewMode());
		} else { 
			status = Status.MODIFY;
			oldContent = git.read(oldRevision, node.getPath(), node.getOldMode());
			newContent = git.read(newRevision, node.getPath(), node.getNewMode());
		}
		return new BlobDiffInfo(status, node.getPath(), node.getPath(), node.getOldMode(), 
				node.getNewMode(), oldRevision, newRevision, oldContent, newContent);
	}
	
	public static BlobDiffInfo from(Git git, FileChange change, String oldRevision, String newRevision) {
		Status status;
		byte[] oldContent, newContent;
		if (change.getStatus() == FileChange.Status.ADD) {
			status = Status.ADD;
			oldContent = null;
			newContent = git.show(newRevision, change.getNewPath());
		} else if (change.getStatus() == FileChange.Status.DELETE) {
			status = Status.DELETE;
			oldContent = git.show(oldRevision, change.getOldPath());
			newContent = null;
		} else if (change.getStatus() == FileChange.Status.MODIFY) {
			status = Status.MODIFY;
			oldContent = git.show(oldRevision, change.getOldPath());
			newContent = git.show(newRevision, change.getNewPath());
		} else {
			status = Status.RENAME;
			oldContent = git.show(oldRevision, change.getOldPath());
			newContent = git.show(newRevision, change.getNewPath());
		}
		
		return new BlobDiffInfo(status, change.getOldPath(), change.getNewPath(), 
				change.getOldMode(), change.getNewMode(), oldRevision, newRevision, 
				oldContent, newContent);
	}
}
