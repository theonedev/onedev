package com.pmease.commons.wicket.behavior.dragdrop;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.pmease.commons.wicket.assets.jqueryui.JQueryUIResourceReference;
import com.pmease.commons.wicket.page.CommonDependentResourceReference;

public class DragDropResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public DragDropResourceReference() {
		super(DragDropResourceReference.class, "dragdrop.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new DragDropCssResourceReference()));
		return dependencies;
	}

}
