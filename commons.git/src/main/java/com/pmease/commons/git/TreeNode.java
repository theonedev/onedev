package com.pmease.commons.git;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.command.ListTreeCommand;

@SuppressWarnings("serial")
public class TreeNode implements Comparable<TreeNode>, Serializable {
	
	private final int modeBits;
	
	private final String path;
	
	private transient String name;
	
	private final String hash;
	
	private final String revision;
	
	private final int size;
	
	private transient FileMode mode;
	
	/**
	 * Construct a tree node.
	 * 
	 * @param modeBits
	 * 			mode bits of the node object. Pass <tt>0</tt> if unknown
	 * @param path
	 * 			path of the node object
	 * @param revision
	 * 			revision of the node object
	 * @param hash
	 * 			hash of the node object
	 * @param size
	 * 			size of the node object 
	 */
	public TreeNode(int modeBits, String path, String revision, String hash, int size) {
		this.path = path;
		this.revision = revision;
		this.hash = hash;
		this.modeBits = modeBits;
		this.size = size;
	}
	
	public String getPath() {
		return path;
	}

	public String getName() {
		if (name == null) {
			if (path.contains("/"))
				name = StringUtils.substringAfterLast(path, "/");
			else
				name = path;
		}
		return name;
	}

	public String getRevision() {
		return revision;
	}
	
	public String getHash() {
		return hash;
	}
	
	public int getModeBits() {
		return modeBits;
	}

	public FileMode getMode() {
		if (mode == null)
			mode = FileMode.fromBits(modeBits);
		return mode;
	}

	public int getSize() {
		return size;
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
	public @Nullable List<TreeNode> listChildren(Git git) {
		if (getMode() == FileMode.TREE) {
			return new ListTreeCommand(git.repoDir()).revision(getRevision()).path(getPath() + "/").call();
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
	public byte[] show(Git git) {
		if (getMode() == FileMode.GITLINK) {
			return git.listSubModules(revision).get(path).getBytes();
		} else {
			return git.show(getRevision(), getPath());
		}
	}

	@Override
	public int compareTo(TreeNode node) {
		if (getMode() == FileMode.TREE) {
			if (node.getMode() == FileMode.TREE)
				return getName().compareTo(node.getName());
			else 
				return -1;
		} else if (node.getMode() == FileMode.TREE) {
			return 1;
		} else {
			return getName().compareTo(node.getName());
		}
	}

}
