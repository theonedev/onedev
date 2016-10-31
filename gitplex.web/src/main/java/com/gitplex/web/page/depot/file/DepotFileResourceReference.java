package com.gitplex.web.page.depot.file;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.web.page.base.BaseDependentResourceReference;
import com.gitplex.commons.wicket.assets.cookies.CookiesResourceReference;
import com.gitplex.commons.wicket.assets.jqueryui.JQueryUIResourceReference;

public class DepotFileResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public DepotFileResourceReference() {
		super(DepotFileResourceReference.class, "depot-file.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(DepotFileResourceReference.class, "depot-file.css")));
		return dependencies;
	}

}
