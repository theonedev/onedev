package com.pmease.commons.wicket.assets.codemirror;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class HighlightResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final HighlightResourceReference INSTANCE = new HighlightResourceReference();
	
	private HighlightResourceReference() {
		super(HighlightResourceReference.class, "highlight.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), ImmutableList.<HeaderItem>of(
					JavaScriptHeaderItem.forReference(CodeMirrorCoreResourceReference.INSTANCE),
					JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(CodeMirrorResourceReference.class, "identifier-highlighter.js")),
					CssHeaderItem.forReference(new CssResourceReference(HighlightResourceReference.class, "highlight.css"))
				));		
	}
	
}

