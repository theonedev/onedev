package com.pmease.gitplex.search.hit;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;

public abstract class QueryHit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String blobPath;
	
	public QueryHit(String blobPath) {
		this.blobPath = blobPath;
	}

	public String getBlobPath() {
		return blobPath;
	}
	
	public abstract int getLineNo();

	public abstract Component render(String componentId);
	
	@Nullable
	public abstract String getScope();
	
	public abstract ResourceReference getIcon();
	
}
