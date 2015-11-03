package com.pmease.commons.wicket.assets.align;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class AlignResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final AlignResourceReference INSTANCE = new AlignResourceReference();
	
	private AlignResourceReference() {
		super(AlignResourceReference.class, "jquery.align.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(AlignResourceReference.class, "jquery.align.css")));
		return dependencies;
	}
	
}
