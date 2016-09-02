package com.pmease.commons.wicket.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

public class CommonDependentCssResourceReference extends CssResourceReference {

	private static final long serialVersionUID = 1L;

	public CommonDependentCssResourceReference(Class<?> scope, String name) {
		super(scope, name);
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(CssHeaderItem.forReference(new CommonCssResourceReference()));
		return dependencies;
	}

}
