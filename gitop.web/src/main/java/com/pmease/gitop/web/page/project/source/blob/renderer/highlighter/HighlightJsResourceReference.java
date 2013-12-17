package com.pmease.gitop.web.page.project.source.blob.renderer.highlighter;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class HighlightJsResourceReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;
	
	public HighlightJsResourceReference() {
		super(HighlightJsResourceReference.class, "highlightjs-highlighter.js");
	}

	private static final ResourceReference HIGHLIGHT_JS = new JavaScriptResourceReference(HighlightJsHighlighter.class, "res/highlight/highlight.pack.js");
	private static final ResourceReference HIGHLIGHT_CSS = new CssResourceReference(HighlightJsHighlighter.class, "res/highlight/styles/idea.css");

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), 
				ImmutableList.of(
						(HeaderItem) JavaScriptHeaderItem.forReference(HIGHLIGHT_JS),
						(HeaderItem) CssHeaderItem.forReference(HIGHLIGHT_CSS)));
	}
	
	private static final JavaScriptResourceReference instance =
			new HighlightJsResourceReference();
	
	public static JavaScriptResourceReference getInstance() {
		return instance;
	}
}
