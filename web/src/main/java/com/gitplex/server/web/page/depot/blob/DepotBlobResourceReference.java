package com.gitplex.server.web.page.depot.blob;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import com.gitplex.server.web.assets.js.cookies.CookiesResourceReference;
import com.gitplex.server.web.assets.js.jqueryui.JQueryUIResourceReference;
import com.gitplex.server.web.page.base.BaseDependentCssResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class DepotBlobResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public DepotBlobResourceReference() {
		super(DepotBlobResourceReference.class, "depot-blob.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(DepotBlobResourceReference.class, "depot-blob.css")));
		return dependencies;
	}

}
