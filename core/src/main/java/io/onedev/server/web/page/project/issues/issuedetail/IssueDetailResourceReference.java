package io.onedev.server.web.page.project.issues.issuedetail;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class IssueDetailResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public IssueDetailResourceReference() {
		super(IssueDetailResourceReference.class, "issue-detail.css");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(
				new BaseDependentResourceReference(IssueDetailResourceReference.class, "issue-detail.js")));
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		return dependencies;
	}

}
