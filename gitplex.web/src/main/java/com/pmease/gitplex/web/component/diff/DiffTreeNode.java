package com.pmease.gitplex.web.component.diff;

import java.io.Serializable;

import com.pmease.commons.git.Change;

@SuppressWarnings("serial")
class DiffTreeNode implements Serializable {
	
	private final Change change;
	
	public DiffTreeNode(Change change) {
		this.change = change;
	}

	public Change getChange() {
		return change;
	}

	@Override
	public int hashCode() {
		return change.getPath().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DiffTreeNode) {
			DiffTreeNode node = (DiffTreeNode) obj;
			return node.getChange().getPath().equals(change.getPath());
		} else {
			return false;
		}
	}
	
}
