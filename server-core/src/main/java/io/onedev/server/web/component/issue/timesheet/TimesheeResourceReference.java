package io.onedev.server.web.component.issue.timesheet;

import io.onedev.server.web.page.base.BaseDependentCssResourceReference;
import io.onedev.server.web.page.base.BaseDependentResourceReference;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;

import java.util.List;

public class TimesheeResourceReference extends BaseDependentResourceReference {
	
	public TimesheeResourceReference() {
		super(TimesheeResourceReference.class, "timesheet.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		var dependencies = super.getDependencies();
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				TimesheeResourceReference.class, "timesheet.css")));
		return dependencies;
	}
}
