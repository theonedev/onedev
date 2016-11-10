package com.gitplex.commons.wicket.component.floating;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.commons.wicket.page.CommonDependentResourceReference;

public class FloatingResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public FloatingResourceReference() {
		super(FloatingResourceReference.class, "floating.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(FloatingResourceReference.class, "floating.css")));
		return dependencies;
	}

}
