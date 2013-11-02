package com.pmease.commons.git;

import java.io.File;

import com.pmease.commons.git.command.ShowCommand;

@SuppressWarnings("serial")
public class FileNode extends TreeNode {
	
	private final long size;

	public FileNode(File gitDir, String path, String revision, String hash, String mode, long size) {
		super(gitDir, path, revision, hash, mode);
		this.size = size;
	}

	public long getSize() {
		return size;
	}

	public byte[] getContent() {
		return new ShowCommand(gitDir).revision(getRevision()).path(getPath()).call();
	}
	
}
