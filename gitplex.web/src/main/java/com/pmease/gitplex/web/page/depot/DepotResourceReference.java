package com.pmease.gitplex.web.page.depot;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.pmease.commons.wicket.assets.cookies.CookiesResourceReference;
import com.pmease.gitplex.web.page.base.BaseDependentResourceReference;

public class DepotResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public DepotResourceReference() {
		super(DepotResourceReference.class, "depot.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(DepotResourceReference.class, "depot.css")));
		return dependencies;
	}

}
