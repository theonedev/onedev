package io.onedev.server.web.websocket;

import java.util.Collection;

import com.google.common.collect.Lists;

public class CompositeRegion implements WebSocketRegion {

	private final Collection<WebSocketRegion> regions;
	
	public CompositeRegion(Collection<WebSocketRegion> regions) {
		this.regions = regions;
	}
	
	public CompositeRegion(WebSocketRegion region1, WebSocketRegion region2) {
		this.regions = Lists.newArrayList(region1, region2);
	}
	
	public CompositeRegion(WebSocketRegion region1, WebSocketRegion region2, WebSocketRegion region3) {
		this.regions = Lists.newArrayList(region1, region2, region3);
	}
	
	@Override
	public boolean contains(WebSocketRegion region) {
		for (WebSocketRegion each: regions) {
			if (each.contains(region))
				return true;
		}
		return false;
	}

}
