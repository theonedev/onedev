package com.gitplex.server.web.page.depot.blob.render.renderers.folder;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class FolderViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public FolderViewResourceReference() {
		super(FolderViewResourceReference.class, "folder-view.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(FolderViewResourceReference.class, "folder-view.css")));
		return dependencies;
	}

}
