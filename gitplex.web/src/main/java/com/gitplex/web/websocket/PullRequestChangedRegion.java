package com.gitplex.web.websocket;

import com.gitplex.commons.wicket.websocket.WebSocketRegion;

public class PullRequestChangedRegion implements WebSocketRegion {
	
	public final Long requestId;

	public PullRequestChangedRegion(Long requestId) {
		this.requestId = requestId;
	}

	@Override
	public boolean contains(WebSocketRegion region) {
		if (region instanceof PullRequestChangedRegion) {
			PullRequestChangedRegion pullRequestChangedRegion = (PullRequestChangedRegion) region;
			return requestId.equals(pullRequestChangedRegion.requestId);
		} else {
			return false;
		}
	}		
	
}