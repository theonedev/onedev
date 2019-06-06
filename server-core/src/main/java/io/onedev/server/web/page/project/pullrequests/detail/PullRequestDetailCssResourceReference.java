package io.onedev.server.web.page.project.pullrequests.detail;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.component.commit.status.CommitStatusCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class PullRequestDetailCssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public PullRequestDetailCssResourceReference() {
		super(PullRequestDetailCssResourceReference.class, "pull-request-detail.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new CommitStatusCssResourceReference()));
		return dependencies;
	}

}
