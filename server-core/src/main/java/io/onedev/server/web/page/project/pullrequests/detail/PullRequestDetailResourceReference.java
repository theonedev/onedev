package io.onedev.server.web.page.project.pullrequests.detail;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.component.commit.status.CommitStatusCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PullRequestDetailResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PullRequestDetailResourceReference() {
		super(PullRequestDetailResourceReference.class, "pull-request-detail.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CommitStatusCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				PullRequestDetailResourceReference.class, "pull-request-detail.css")));
		return dependencies;
	}

}
