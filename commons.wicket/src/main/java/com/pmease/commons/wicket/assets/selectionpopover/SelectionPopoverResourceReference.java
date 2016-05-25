package com.pmease.commons.wicket.assets.selectionpopover;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.align.AlignResourceReference;

public class SelectionPopoverResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final SelectionPopoverResourceReference INSTANCE = new SelectionPopoverResourceReference();
	
	private SelectionPopoverResourceReference() {
		super(SelectionPopoverResourceReference.class, "jquery.selectionpopover.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(AlignResourceReference.INSTANCE));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				SelectionPopoverResourceReference.class, "jquery.selectionpopover.css")));
		return dependencies;
	}
	
}
