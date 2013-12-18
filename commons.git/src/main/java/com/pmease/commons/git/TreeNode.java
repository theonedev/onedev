package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.command.ListTreeCommand;

@SuppressWarnings("serial")
public class TreeNode implements Serializable {

	protected final File gitDir;
	
	private final FileMode mode;
	
	private final String path;
	
	private final String hash;
	
	private final String revision;
	
	private final int size;
	
	private Optional<TreeNode> parentNode;
	
	public TreeNode(File gitDir, FileMode mode, String path, String revision, String hash, int size) {
		this.gitDir = gitDir;
		this.path = path;
		this.revision = revision;
		this.hash = hash;
		this.mode = mode;
		this.size = size;
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
	
	public FileMode getMode() {
		return mode;
	}

	public int getSize() {
		return size;
	}

	/**
	 * Get parent node of current node. 
	 *  
	 * @return
	 * 			parent node of current node, or <tt>null</tt> if current node locates directly 
	 * 			under the repository root
	 */
	public @Nullable TreeNode getParent() {
		if (parentNode == null) {
			if (path.contains("/")) {
				String parentPath = StringUtils.substringBeforeLast(path, "/");
				List<TreeNode> result = new ListTreeCommand(gitDir).revision(getRevision()).path(parentPath).call();
				Preconditions.checkArgument(result.size() == 1);
				parentNode = Optional.of(result.get(0));
			} else {
				parentNode = Optional.fromNullable(null);
			}
		}
		return parentNode.orNull();
	}
	
	public void setParent(@Nullable TreeNode parent) {
		this.parentNode = Optional.fromNullable(parent);
	}

	@Override
	public String toString() {
		return getPath();
	}
	
	/**
	 * List child nodes of current node. 
	 * 
	 * @return
	 * 			child nodes of current node, or <tt>null</tt> if current node does not represent a 
	 * 			directory. 
	 */
	public @Nullable List<TreeNode> listChildren() {
		if (mode == FileMode.TREE) {
			List<TreeNode> children = new ListTreeCommand(gitDir).revision(getRevision()).path(getPath() + "/").call();
			for (TreeNode each: children) {
				each.setParent(this);
			}
			return children;
		} else {
			return null;
		}
	}
	
	/**
	 * Show content of current node. 
	 * 
	 * @return
	 * 			content of the file if current node is a file, or URL of the submodule if 
	 * 			current node represents a submodule, or target path if current node 
	 * 			represents a symbol link, or contents of the directory if current node 
	 * 			represents a directory
	 */
	public byte[] show() {
		Git git = new Git(gitDir);
		if (mode == FileMode.GITLINK) {
			return git.listSubModules(revision).get(path).getBytes();
		} else {
			return git.show(getRevision(), getPath());
		}
	}

}
