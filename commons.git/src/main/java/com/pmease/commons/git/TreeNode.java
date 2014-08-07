package com.pmease.commons.git;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.FileMode;

@SuppressWarnings("serial")
public class TreeNode implements Comparable<TreeNode>, Serializable {
	
	private final int mode;
	
	private final String path;
	
	private transient String name;
	
	private final String hash;
	
	private final int size;
	
	/**
	 * Construct a tree node.
	 * 
	 * @param modeBits
	 * 			mode bits of the node object. Pass <tt>0</tt> if unknown
	 * @param path
	 * 			path of the node object
	 * @param hash
	 * 			hash of the node object
	 * @param size
	 * 			size of the node object 
	 */
	public TreeNode(int modeBits, String path, String hash, int size) {
		this.path = path;
		this.hash = hash;
		this.mode = modeBits;
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

	public String getHash() {
		return hash;
	}
	
	public int getMode() {
		return mode;
	}

	public boolean isFolder() {
		return mode == FileMode.TYPE_TREE;
	}

	public int getSize() {
		return size;
	}

	@Override
	public String toString() {
		return getPath();
	}
	
	@Override
	public int compareTo(TreeNode node) {
		if (getMode() == FileMode.TYPE_TREE) {
			if (node.getMode() == FileMode.TYPE_TREE)
				return getName().compareTo(node.getName());
			else 
				return -1;
		} else if (node.getMode() == FileMode.TYPE_TREE) {
			return 1;
		} else {
			return getName().compareTo(node.getName());
		}
	}

}
