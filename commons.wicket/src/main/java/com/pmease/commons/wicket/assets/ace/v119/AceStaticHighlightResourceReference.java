package com.pmease.commons.wicket.assets.ace.v119;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.VersionlessJavaScriptResourceReference;

public class AceStaticHighlightResourceReference extends VersionlessJavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AceStaticHighlightResourceReference INSTANCE = new AceStaticHighlightResourceReference();
	
	private AceStaticHighlightResourceReference() {
		super(AceStaticHighlightResourceReference.class, "src-noconflict/ext-static_highlight.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Lists.newArrayList(JavaScriptHeaderItem.forReference(AceResourceReference.INSTANCE));
	}

}
