package com.gitplex.search.hit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.commons.util.Range;
import com.gitplex.commons.wicket.component.EmphasizeAwareLabel;

public class FileHit extends QueryHit {

	private static final long serialVersionUID = 1L;

	private final Range matchRange;
	
	public FileHit(String blobPath, @Nullable Range matchRange) {
		super(blobPath, null);
		this.matchRange = matchRange;
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
		
		return new EmphasizeAwareLabel(componentId, fileName, matchRange);
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

	public Range getMatchRange() {
		return matchRange;
	}

	@Override
	protected int score() {
		if (getBlobPath().contains("/")) 
			return StringUtils.substringAfterLast(getBlobPath(), "/").length();
		else 
			return getBlobPath().length();
	}

}
