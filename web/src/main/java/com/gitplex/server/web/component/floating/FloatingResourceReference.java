package com.gitplex.server.web.component.floating;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class FloatingResourceReference extends BaseDependentResourceReference {

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
