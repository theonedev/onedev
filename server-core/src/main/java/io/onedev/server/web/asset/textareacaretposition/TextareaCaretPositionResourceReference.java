package io.onedev.server.web.asset.textareacaretposition;

import java.io.Serial;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class TextareaCaretPositionResourceReference extends JavaScriptResourceReference {

	@Serial
	private static final long serialVersionUID = 1L;

	public TextareaCaretPositionResourceReference() {
		super(TextareaCaretPositionResourceReference.class, "textarea-caret-position.js");
	}

}
