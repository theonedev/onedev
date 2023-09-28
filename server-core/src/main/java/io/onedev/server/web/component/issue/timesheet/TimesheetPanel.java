package io.onedev.server.web.component.issue.timesheet;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.util.Day;
import org.apache.wicket.markup.html.panel.Panel;

import javax.annotation.Nullable;

public abstract class TimesheetPanel extends Panel {
	
	public TimesheetPanel(String id) {
		super(id);
	}
	
	@Nullable
	protected abstract Project getProject();
	
	protected abstract TimesheetSetting getSetting();
	
	protected abstract Day getFromDay();
	
	protected abstract Day getToDay();
	
}
