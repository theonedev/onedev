package com.gitplex.server.web.page.project;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.assets.js.cookies.CookiesResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class ProjectResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public ProjectResourceReference() {
		super(ProjectResourceReference.class, "project.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(ProjectResourceReference.class, "project.css")));
		return dependencies;
	}

}
