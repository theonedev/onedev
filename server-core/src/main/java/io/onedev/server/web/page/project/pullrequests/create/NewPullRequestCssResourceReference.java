package io.onedev.server.web.page.project.pullrequests.create;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.asset.revisioncompare.RevisionCompareCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;

public class NewPullRequestCssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public NewPullRequestCssResourceReference() {
		super(NewPullRequestCssResourceReference.class, "new-pull-request.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new RevisionCompareCssResourceReference()));
		return dependencies;
	}

}
