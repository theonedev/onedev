package com.pmease.commons.wicket.assets.textareacaretposition;

import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class TextareaCaretPositionResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final TextareaCaretPositionResourceReference INSTANCE = new TextareaCaretPositionResourceReference();
	
	private TextareaCaretPositionResourceReference() {
		super(TextareaCaretPositionResourceReference.class, "textarea-caret-position.js");
	}

}
