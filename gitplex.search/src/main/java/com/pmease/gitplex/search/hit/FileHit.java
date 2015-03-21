package com.pmease.gitplex.search.hit;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;

public class FileHit extends QueryHit {

	public FileHit(String blobPath) {
		super(blobPath);
	}

	@Override
	public String toString() {
		return getBlobPath();
	}

	@Override
	public Component render(String componentId) {
		return new Label(componentId, getBlobPath());
	}

}
