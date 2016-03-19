package com.pmease.gitplex.web.component.commitgraph;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.pmease.commons.wicket.assets.snapsvg.SnapSvgResourceReference;

public class CommitGraphResourceReference extends JavaScriptResourceReference {

	private static final long serialVersionUID = 1L;

	public static final CommitGraphResourceReference INSTANCE = new CommitGraphResourceReference();
	
	private CommitGraphResourceReference() {
		super(CommitGraphResourceReference.class, "commit-graph.js");
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = new ArrayList<>();
		dependencies.add(JavaScriptHeaderItem.forReference(SnapSvgResourceReference.INSTANCE));
		dependencies.add(CssHeaderItem.forReference(
				new CssResourceReference(CommitGraphResourceReference.class, "commit-graph.css")));
		return dependencies;
	}

}
