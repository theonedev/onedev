package com.gitplex.web.page.depot.commit;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.web.page.base.BaseDependentResourceReference;
import com.gitplex.commons.wicket.assets.oneline.OnelineResourceReference;

public class CommitDetailResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitDetailResourceReference() {
		super(CommitDetailResourceReference.class, "commit-detail.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new OnelineResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(CommitDetailResourceReference.class, "commit-detail.css")));
		return dependencies;
	}

}
