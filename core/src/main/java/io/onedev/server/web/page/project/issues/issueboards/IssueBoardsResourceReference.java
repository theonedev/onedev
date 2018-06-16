package io.onedev.server.web.page.project.issues.issueboards;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.onedev.server.web.asset.perfectscrollbar.PerfectScrollbarResourceReference;
import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class IssueBoardsResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;
	
	public IssueBoardsResourceReference() {
		super(IssueBoardsResourceReference.class, "issue-boards.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new PerfectScrollbarResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				IssueBoardsResourceReference.class, "issue-boards.css")));
		return dependencies;
	}

}
