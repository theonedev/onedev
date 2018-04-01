package io.onedev.server.web.page.project.blob.render.commitoption;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class CommitOptionResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitOptionResourceReference() {
		super(CommitOptionResourceReference.class, "commit-option.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CommitOptionResourceReference.class, "commit-option.css")));
		return dependencies;
	}

}
