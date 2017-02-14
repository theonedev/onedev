package com.gitplex.server.web.page.depot.pullrequest.requestdetail.changes;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class RequestChangesResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public RequestChangesResourceReference() {
		super(RequestChangesResourceReference.class, "request-changes.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(RequestChangesResourceReference.class, "request-changes.css")));
		return dependencies;
	}

}
