package com.pmease.commons.wicket.assets.doneevents;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DoneEventsResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final DoneEventsResourceReference INSTANCE = new DoneEventsResourceReference();
	
	private DoneEventsResourceReference() {
		super(DoneEventsResourceReference.class, "jquery.doneevents.js");
	}

}
