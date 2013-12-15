package com.pmease.gitop.web.page.project.source.blob.renderer.syntax;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class HighlightJavaScriptResourceReference extends JavaScriptResourceReference {
	private static final long serialVersionUID = 1L;
	
	public HighlightJavaScriptResourceReference() {
		super(HighlightJavaScriptResourceReference.class, "source-highlight.js");
	}

	private static final ResourceReference HIGHLIGHT_JS = new JavaScriptResourceReference(HighlightBehavior.class, "res/highlight/highlight.pack.js");
	private static final ResourceReference HIGHLIGHT_OVERRIDES_JS = new JavaScriptResourceReference(HighlightBehavior.class, "res/highlight/highlight-overrides.js");
	private static final ResourceReference HIGHLIGHT_CSS = new CssResourceReference(HighlightBehavior.class, "res/highlight/styles/idea.css");

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), 
				ImmutableList.of(
						(HeaderItem) JavaScriptHeaderItem.forReference(HIGHLIGHT_JS),
						(HeaderItem) JavaScriptHeaderItem.forReference(HIGHLIGHT_OVERRIDES_JS),
						(HeaderItem) CssHeaderItem.forReference(HIGHLIGHT_CSS)));
	}
	
	private static final JavaScriptResourceReference instance =
			new HighlightJavaScriptResourceReference();
	
	public static JavaScriptResourceReference getInstance() {
		return instance;
	}
}
