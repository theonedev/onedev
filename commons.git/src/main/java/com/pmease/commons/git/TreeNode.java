package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.pmease.commons.git.command.ListTreeCommand;
import com.pmease.commons.git.command.ShowCommand;

@SuppressWarnings("serial")
public class TreeNode implements Serializable {

	public enum Type {
		DIRECTORY, FILE, SUBMODULE, SYMBOLLINK;
		
		public static Type fromMode(String mode) {
			if (mode.startsWith("040"))
				return DIRECTORY;
			else if (mode.startsWith("160"))
				return SUBMODULE;
			else if (mode.startsWith("120"))
				return SYMBOLLINK;
			else 
				return FILE;
		}
		
	}
	
	protected final File gitDir;
	
	private final Type type;
	
	private final String path;
	
	private final String hash;
	
	private final String revision;
	
	private final int size;
	
	private Optional<TreeNode> parentNode;
	
	public TreeNode(File gitDir, Type type, String path, String revision, String hash, int size) {
		this.gitDir = gitDir;
		this.path = path;
		this.revision = revision;
		this.hash = hash;
		this.type = type;
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
	
	public Type getType() {
		return type;
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
		if (type == Type.DIRECTORY) {
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
		if (type == Type.SUBMODULE) {
			return new Git(gitDir).listSubModules(revision).get(path).getBytes();
		} else {
			return new ShowCommand(gitDir).revision(getRevision()).path(getPath()).call();
		}
	}

}
