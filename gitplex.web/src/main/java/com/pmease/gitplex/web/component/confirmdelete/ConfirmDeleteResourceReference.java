package com.pmease.gitplex.web.component.confirmdelete;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.gitplex.web.page.base.BaseDependentResourceReference;

public class ConfirmDeleteResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ConfirmDeleteResourceReference() {
		super(ConfirmDeleteResourceReference.class, "confirm-delete.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ConfirmDeleteResourceReference.class, "confirm-delete.css")));
		return dependencies;
	}

}
