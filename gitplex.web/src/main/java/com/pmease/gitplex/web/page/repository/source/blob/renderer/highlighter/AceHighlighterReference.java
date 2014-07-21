package com.pmease.gitplex.web.page.repository.source.blob.renderer.highlighter;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class AceHighlighterReference extends JavaScriptResourceReference {

	private static final AceHighlighterReference instance = new AceHighlighterReference();
	
	public AceHighlighterReference() {
		super(AceHighlighterReference.class, "acehighlighter.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(),
				ImmutableList.of(
						JavaScriptHeaderItem.forUrl("assets/js/vendor/ace-noconflict/ace.js"),
						JavaScriptHeaderItem.forUrl("assets/js/vendor/ace-noconflict/ext-static_highlight.js")
						));
	}
	
	public static AceHighlighterReference instance() {
		return instance;
	}
}
