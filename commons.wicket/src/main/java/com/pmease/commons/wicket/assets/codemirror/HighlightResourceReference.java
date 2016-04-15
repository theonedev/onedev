package com.pmease.commons.wicket.assets.codemirror;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class HighlightResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final HighlightResourceReference INSTANCE = new HighlightResourceReference();
	
	private HighlightResourceReference() {
		super(HighlightResourceReference.class, "highlight.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(CodeMirrorCoreResourceReference.INSTANCE));
		dependencies.add(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(CodeMirrorResourceReference.class, "identifier-highlighter.js")));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(HighlightResourceReference.class, "highlight.css")));
		return dependencies;
	}
	
}

