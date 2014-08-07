package com.pmease.gitplex.web.component.view;

import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;

public class BlobRenderInfo {
	
	private final String path;
	
	private final String revision;
	
	private final int mode;
	
	private final byte[] content;
	
	public BlobRenderInfo(String path, String revision, int mode, byte[] content) {
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

	public int getMode() {
		return mode;
	}

	public byte[] getContent() {
		return content;
	}
	
	public static BlobRenderInfo from(Git git, TreeNode node, String revision) {
		Preconditions.checkArgument(node.getMode() != FileMode.TYPE_TREE);
		
		byte[] content = git.read(revision, node.getPath(), node.getMode());
		return new BlobRenderInfo(node.getPath(), revision, node.getMode(), content);
	}
}
