package io.onedev.server.web.component.issue.create;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class NewIssueResourceReference extends BaseDependentResourceReference {

	public NewIssueResourceReference() {
		super(NewIssueResourceReference.class, "new-issue.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = new ArrayList<HeaderItem>();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
			NewIssueEditor.class, "new-issue.css")));
		return dependencies;
	}
}
