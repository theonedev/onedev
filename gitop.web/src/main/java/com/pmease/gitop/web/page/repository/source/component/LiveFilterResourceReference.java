package com.pmease.gitop.web.page.repository.source.component;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class LiveFilterResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public LiveFilterResourceReference() {
		super(LiveFilterResourceReference.class, "res/js/jquery.livefilter.js");
	}

	private static LiveFilterResourceReference instance =
			new LiveFilterResourceReference();
	
	public static LiveFilterResourceReference get() {
		return instance;
	}
}
