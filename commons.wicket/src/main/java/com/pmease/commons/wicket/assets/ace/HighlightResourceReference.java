package com.pmease.commons.wicket.assets.ace;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.pmease.commons.wicket.VersionlessJavaScriptResourceReference;

public class HighlightResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final HighlightResourceReference INSTANCE = new HighlightResourceReference();
	
	private HighlightResourceReference() {
		super(HighlightResourceReference.class, "jquery.highlight.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Iterables.concat(super.getDependencies(), ImmutableList.of(
					JavaScriptHeaderItem.forReference(AceResourceReference.INSTANCE),
					JavaScriptHeaderItem.forReference(new VersionlessJavaScriptResourceReference(HighlightResourceReference.class, "v20141220/noconflict/ext-static_highlight.js")))
				);
	}

}
