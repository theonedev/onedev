package com.pmease.gitplex.web.component.diff;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.DiffTreeNode;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;

public class BlobInfo {
	
	private final String path;
	
	private final int mode;
	
	private String revision;
	
	private final byte[] content;

	public BlobInfo(String path, String revision, int mode, byte[] content) {
		this.path = path;
		this.revision = revision;
		this.mode = mode;
		this.content = content;
	}
	
	public String getPath() {
		return path;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public int getMode() {
		return mode;
	}

	public byte[] getContent() {
		return content;
	}
	
	public static @Nullable BlobInfo fromDiffTreeNode(Git git, DiffTreeNode node, String revision, boolean original) {
		Preconditions.checkArgument(!node.isFolder());
		
		if (original) {
			if (node.getStatus() == DiffTreeNode.Status.ADD) {
				return null;
			} else if (node.getOldMode() == FileMode.TYPE_GITLINK) {
				return new BlobInfo(node.getPath(), revision, node.getOldMode(), 
						git.readSubModule(revision, node.getPath()).getBytes());
			} else {
				return new BlobInfo(node.getPath(), revision, node.getOldMode(), 
						git.show(revision, node.getPath()));
			}
		} else {
			if (node.getStatus() == DiffTreeNode.Status.DELETE) {
				return null;
			} else if (node.getNewMode() == FileMode.TYPE_GITLINK) {
				return new BlobInfo(node.getPath(), revision, node.getNewMode(), 
						git.readSubModule(revision, node.getPath()).getBytes());
			} else {
				return new BlobInfo(node.getPath(), revision, node.getNewMode(), 
						git.show(revision, node.getPath()));
			}
		}
	}
	
	public static BlobInfo fromTreeNode(Git git, TreeNode node, String revision) {
		Preconditions.checkArgument(node.isFolder());
		return new BlobInfo(node.getPath(), revision, node.getMode(), node.readContent(git, revision));
	}
	
}
