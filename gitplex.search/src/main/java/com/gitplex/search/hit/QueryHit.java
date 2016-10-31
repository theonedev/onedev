package com.gitplex.search.hit;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;

import com.gitplex.commons.lang.extractors.TokenPosition;

public abstract class QueryHit implements Serializable, Comparable<QueryHit> {
	
	private static final long serialVersionUID = 1L;
	
	private final String blobPath;
	
	private final TokenPosition tokenPos;
	
	private transient Integer score;
	
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
	public abstract String getScope();
	
	public abstract ResourceReference getIcon();
	
	protected abstract int score();
	
	private int getScore() {
		if (score == null) 
			score = score();
		return score;
	}

	@Override
	public int compareTo(QueryHit hit) {
		return getScore() - hit.getScore();
	}
	
}
