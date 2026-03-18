package io.onedev.server.web.page.project.pullrequests.detail.airuns;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.xterm.XtermResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class PullRequestAiRunsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public PullRequestAiRunsResourceReference() {
		super(PullRequestAiRunsResourceReference.class, "pull-request-ai-runs.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new XtermResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				PullRequestAiRunsResourceReference.class, "pull-request-ai-runs.css")));
		return dependencies;
	}

}
