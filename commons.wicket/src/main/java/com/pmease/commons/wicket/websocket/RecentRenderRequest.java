package com.pmease.commons.wicket.websocket;

import java.util.Date;

public class RecentRenderRequest {
	
	private final WebSocketRegion region;
	
	private final PageKey sourcePageKey;
	
	private final PageKey targetPageKey;
	
	private final Date date;
	
	public RecentRenderRequest(WebSocketRegion region, 
			PageKey sourcePageKey, PageKey targetPageKey, Date date) {
		this.region = region;
		this.sourcePageKey = sourcePageKey;
		this.targetPageKey = targetPageKey;
		this.date = date;
	}

	public WebSocketRegion getRegion() {
		return region;
	}

	public PageKey getSourcePageKey() {
		return sourcePageKey;
	}

	public PageKey getTargetPageKey() {
		return targetPageKey;
	}

	public Date getDate() {
		return date;
	}

}