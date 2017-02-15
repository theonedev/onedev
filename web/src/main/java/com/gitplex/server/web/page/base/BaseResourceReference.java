package com.gitplex.server.web.page.base;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import com.gitplex.server.web.page.CommonDependentResourceReference;

public class BaseResourceReference extends CommonDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BaseResourceReference() {
		super(BaseResourceReference.class, "base.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseCssResourceReference()));
		return dependencies;
	}

}
