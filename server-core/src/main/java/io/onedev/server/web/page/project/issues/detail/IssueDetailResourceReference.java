package io.onedev.server.web.page.project.issues.detail;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class IssueDetailResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public IssueDetailResourceReference() {
		super(IssueDetailResourceReference.class, "issue-detail.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
			IssueDetailResourceReference.class, "issue-detail.css")));
		return dependencies;
	}
	
}
