package io.onedev.server.web.asset.clipboard;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ClipboardResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public ClipboardResourceReference() {
		super(ClipboardResourceReference.class, "clipboard.min.js");
	}

}
