package com.pmease.gitop.web.common.wicket.component.dropzone;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class DropZoneResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public DropZoneResourceReference() {
		super(DropZoneResourceReference.class, "dropzone.js");
	}

	private static final DropZoneResourceReference instance =
			new DropZoneResourceReference();
	
	public static DropZoneResourceReference get() {
		return instance;
	}
}
