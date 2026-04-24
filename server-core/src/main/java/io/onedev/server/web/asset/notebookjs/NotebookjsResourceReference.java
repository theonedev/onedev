package io.onedev.server.web.asset.notebookjs;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class NotebookjsResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public NotebookjsResourceReference() {
		super(NotebookjsResourceReference.class, "notebook.js");
	}

}
