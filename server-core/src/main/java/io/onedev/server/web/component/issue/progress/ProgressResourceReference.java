package io.onedev.server.web.component.issue.progress;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import java.util.List;

public class ProgressResourceReference extends BaseDependentResourceReference {
	public ProgressResourceReference() {
		super(ProgressResourceReference.class, "progress.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				ProgressResourceReference.class, "progress.css")));
		return dependencies;
	}
}
