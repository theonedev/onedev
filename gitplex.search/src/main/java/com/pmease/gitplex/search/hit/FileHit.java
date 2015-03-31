package com.pmease.gitplex.search.hit;

import org.apache.wicket.Component;

public class FileHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	public FileHit(String blobPath) {
		super(blobPath);
	}

	@Override
	public String toString() {
		return getBlobPath();
	}

	@Override
	public Component render(String componentId) {
		return new FileHitPanel(componentId, this);
	}

	@Override
	public int getLineNo() {
		return 0;
	}

}
