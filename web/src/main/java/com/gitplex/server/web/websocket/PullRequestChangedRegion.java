package com.gitplex.server.web.websocket;

import com.gitplex.server.web.websocket.WebSocketRegion;

public class PullRequestChangedRegion implements WebSocketRegion {
	
	public final Long requestId;

	public PullRequestChangedRegion(Long requestId) {
		this.requestId = requestId;
	}

	@Override
	public boolean contains(WebSocketRegion region) {
		if (region instanceof PullRequestChangedRegion) {
			PullRequestChangedRegion pageDataChangedRegion = (PullRequestChangedRegion) region;
			return requestId.equals(pageDataChangedRegion.requestId);
		} else {
			return false;
		}
	}		
	
}