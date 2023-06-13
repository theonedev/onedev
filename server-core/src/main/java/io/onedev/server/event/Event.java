package io.onedev.server.event;

import io.onedev.server.web.util.WicketUtils;
import io.onedev.server.web.websocket.PageKey;

import javax.annotation.Nullable;
import java.io.Serializable;

public abstract class Event {
	
	private final PageKey sourcePage;
	
	public Event() {
		sourcePage = WicketUtils.getPageKey(); 
	}

	@Nullable
	public PageKey getSourcePage() {
		return sourcePage;
	}
	
}
