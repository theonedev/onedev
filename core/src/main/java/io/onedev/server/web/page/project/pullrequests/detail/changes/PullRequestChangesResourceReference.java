package io.onedev.server.web.page.project.pullrequests.detail.changes;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.request.resource.CssResourceReference;

import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PullRequestChangesResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PullRequestChangesResourceReference() {
		super(PullRequestChangesResourceReference.class, "pull-request-changes.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CssResourceReference(
				PullRequestChangesResourceReference.class, "pull-request-changes.css")));
		return dependencies;
	}

}
