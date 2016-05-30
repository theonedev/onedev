package com.pmease.commons.wicket.assets.hover;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.align.AlignResourceReference;

public class HoverResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final HoverResourceReference INSTANCE = new HoverResourceReference();
	
	private HoverResourceReference() {
		super(HoverResourceReference.class, "jquery.hover.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(AlignResourceReference.INSTANCE));
		return dependencies;
	}
	
}
