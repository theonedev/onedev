package io.onedev.server.ee.xsearch;

import io.onedev.server.event.Event;

public class CodeIndexStatusChanged extends Event {
	
	private static final long serialVersionUID = 1L;
	
	public static String getChangeObservable() {
		return CodeIndexStatusChanged.class.getName();
	}
	
}
