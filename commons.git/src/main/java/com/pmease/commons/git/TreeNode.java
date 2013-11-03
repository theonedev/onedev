package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.command.ListTreeCommand;

@SuppressWarnings("serial")
public class TreeNode implements Serializable {
	
	protected final File gitDir;
	
	private final String mode;
	
	private final String path;
	
	private final String hash;
	
	private final String revision;
	
	private Optional<DirNode> parentNode;
	
	public TreeNode(File gitDir, String path, String revision, String hash, String mode) {
		this.gitDir = gitDir;
		this.path = path;
		this.revision = revision;
		this.hash = hash;
		this.mode = mode;
	}
	
	public String getPath() {
		return path;
	}

	public String getName() {
		if (path.contains("/"))
			return StringUtils.substringAfterLast(path, "/");
		else
			return path;
	}

	public String getRevision() {
		return revision;
	}
	
	public String getHash() {
		return hash;
	}
	
	public String getMode() {
		return mode;
	}

	public @Nullable TreeNode getParent() {
		if (parentNode == null) {
			if (path.contains("/")) {
				String parentPath = StringUtils.substringBeforeLast(path, "/");
				List<TreeNode> result = new ListTreeCommand(gitDir).revision(getRevision()).path(parentPath).call();
				Preconditions.checkArgument(result.size() == 1);
				parentNode = Optional.of((DirNode)result.get(0));
			} else {
				parentNode = Optional.fromNullable(null);
			}
		}
		return parentNode.orNull();
	}
	
	public void setParent(@Nullable DirNode parent) {
		this.parentNode = Optional.fromNullable(parent);
	}

	@Override
	public String toString() {
		return getPath();
	}
	
}
