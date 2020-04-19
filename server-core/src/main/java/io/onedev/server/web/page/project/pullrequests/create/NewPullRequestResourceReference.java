package io.onedev.server.web.page.project.pullrequests.create;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.asset.revisioncompare.RevisionCompareCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class NewPullRequestResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public NewPullRequestResourceReference() {
		super(NewPullRequestResourceReference.class, "new-pull-request.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new RevisionCompareCssResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				NewPullRequestResourceReference.class, "new-pull-request.css")));
		return dependencies;
	}

}
