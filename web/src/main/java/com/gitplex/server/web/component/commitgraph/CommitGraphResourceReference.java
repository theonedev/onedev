package com.gitplex.server.web.component.commitgraph;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import com.gitplex.server.web.assets.js.snapsvg.SnapSvgResourceReference;
import com.gitplex.server.web.page.base.BaseDependentResourceReference;

public class CommitGraphResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitGraphResourceReference() {
		super(CommitGraphResourceReference.class, "commit-graph.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new SnapSvgResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CommitGraphResourceReference.class, "commit-graph.css")));
		return dependencies;
	}

}
