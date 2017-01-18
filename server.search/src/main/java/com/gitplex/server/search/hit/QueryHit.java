package com.gitplex.server.search.hit;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;

import com.gitplex.jsymbol.TokenPosition;

public abstract class QueryHit implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private final String blobPath;
	
	private final TokenPosition tokenPos;
	
	public QueryHit(String blobPath, @Nullable TokenPosition tokenPos) {
		this.blobPath = blobPath;
		this.tokenPos = tokenPos;
	}

	public String getBlobPath() {
		return blobPath;
	}
	
	@Nullable
	public TokenPosition getTokenPos() {
		return tokenPos;
	}

	public abstract Component render(String componentId);
	
	@Nullable
	public abstract String getNamespace();
	
	public abstract Image renderIcon(String componentId);
	
}
