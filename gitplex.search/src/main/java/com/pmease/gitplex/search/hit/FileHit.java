package com.pmease.gitplex.search.hit;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class FileHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	public FileHit(String blobPath) {
		super(blobPath, null);
	}

	@Override
	public String toString() {
		return getBlobPath();
	}

	@Override
	public Component render(String componentId) {
		String fileName = getBlobPath();
		if (fileName.contains("/")) 
			fileName = StringUtils.substringAfterLast(fileName, "/");
		
		return new Label(componentId, fileName);
	}

	@Override
	public ResourceReference getIcon() {
		return new PackageResourceReference(FileHit.class, "file.png");
	}

	@Override
	public String getScope() {
		if (getBlobPath().contains("/")) 
			return StringUtils.substringBeforeLast(getBlobPath(), "/");
		else 
			return null;
	}

	@Override
	protected int score() {
		if (getBlobPath().contains("/")) 
			return StringUtils.substringAfterLast(getBlobPath(), "/").length();
		else 
			return getBlobPath().length();
	}

}
