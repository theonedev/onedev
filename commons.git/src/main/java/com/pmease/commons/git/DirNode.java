package com.pmease.commons.git;

import java.io.File;
import java.util.List;

import com.pmease.commons.git.command.ListTreeCommand;

@SuppressWarnings("serial")
public class DirNode extends TreeNode {

	public DirNode(File gitDir, String path, String revision, String hash, String mode) {
		super(gitDir, path, revision, hash, mode);
	}

	public List<TreeNode> listChildren() {
		List<TreeNode> children = new ListTreeCommand(gitDir).revision(getRevision()).path(getPath() + "/").call();
		for (TreeNode each: children) {
			each.setParent(this);
		}
		return children;
	}
	
}
