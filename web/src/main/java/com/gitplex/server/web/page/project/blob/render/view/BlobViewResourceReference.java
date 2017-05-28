package com.gitplex.server.web.page.project.blob.render.view;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class BlobViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public BlobViewResourceReference() {
		super(BlobViewResourceReference.class, "blob-view.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(BlobViewResourceReference.class, "blob-view.css")));
		return dependencies;
	}

}
