package io.onedev.server.web.component.progress;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;

public class TimeProgressResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public TimeProgressResourceReference() {
		super(TimeProgressResourceReference.class, "time-progress.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				TimeProgressResourceReference.class, "time-progress.css")));
		return dependencies;
	}
}
