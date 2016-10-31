package com.gitplex.web.component.depotfile.blobsearch.result;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.web.page.base.BaseDependentResourceReference;
import com.gitplex.commons.wicket.assets.uri.URIResourceReference;

public class SearchResultResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SearchResultResourceReference() {
		super(SearchResultResourceReference.class, "search-result.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new URIResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(SearchResultResourceReference.class, "search-result.css")));
		return dependencies;
	}

}
