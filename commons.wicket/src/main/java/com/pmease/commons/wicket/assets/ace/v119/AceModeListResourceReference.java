package com.pmease.commons.wicket.assets.ace.v119;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.VersionlessJavaScriptResourceReference;

public class AceModeListResourceReference extends VersionlessJavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AceModeListResourceReference INSTANCE = new AceModeListResourceReference();
	
	private AceModeListResourceReference() {
		super(AceModeListResourceReference.class, "src-noconflict/ext-modelist.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		return Lists.newArrayList(JavaScriptHeaderItem.forReference(AceResourceReference.INSTANCE));
	}

}
