package io.onedev.server.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.websocket.PageKey;

import org.jspecify.annotations.Nullable;

public abstract class Event {
	
	@JsonIgnore
	private final PageKey sourcePage;
	
	public Event() {
		sourcePage = WicketUtils.getPageKey(); 
	}
	
	@Nullable
	public PageKey getSourcePage() {
		return sourcePage;
	}
	
}
