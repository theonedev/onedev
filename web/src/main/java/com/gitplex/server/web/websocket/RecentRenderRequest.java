package com.gitplex.server.web.websocket;

import java.util.Date;

public class RecentRenderRequest {
	
	private final WebSocketRegion region;
	
	private final PageKey sourcePageKey;
	
	private final Date date;
	
	public RecentRenderRequest(WebSocketRegion region, PageKey sourcePageKey, Date date) {
		this.region = region;
		this.sourcePageKey = sourcePageKey;
		this.date = date;
	}

	public WebSocketRegion getRegion() {
		return region;
	}

	public PageKey getSourcePageKey() {
		return sourcePageKey;
	}

	public Date getDate() {
		return date;
	}

}