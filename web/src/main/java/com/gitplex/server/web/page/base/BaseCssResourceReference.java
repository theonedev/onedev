package com.gitplex.server.web.page.base;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import com.gitplex.server.web.page.CommonDependentCssResourceReference;
import com.gitplex.server.web.page.base.fontext.FontExtResourceReference;

public class BaseCssResourceReference extends CommonDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public BaseCssResourceReference() {
		super(BaseCssResourceReference.class, "base.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new FontExtResourceReference()));
		return dependencies;
	}

}
