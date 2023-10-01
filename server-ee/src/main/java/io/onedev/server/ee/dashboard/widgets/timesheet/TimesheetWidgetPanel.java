package io.onedev.server.ee.dashboard.widgets.timesheet;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.web.component.issue.timesheet.DateRangeNavigator;
import io.onedev.server.web.component.issue.timesheet.TimesheetPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public abstract class TimesheetWidgetPanel extends Panel {
	
	private LocalDate baseDate;
	
	private Component body;
	
	public TimesheetWidgetPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new DateRangeNavigator("dateRangeNav") {
			
			@Override
			protected LocalDate getBaseDate() {
				return baseDate;
			}

			@Override
			protected void onBaseDateUpdate(AjaxRequestTarget target, LocalDate baseDate) {
				TimesheetWidgetPanel.this.baseDate = baseDate;
				target.add(body);
			}

			@Override
			protected TimesheetSetting getTimesheetSetting() {
				return getSetting();
			}
			
		});
		
		add(body = new TimesheetPanel("body") {
			
			@Override
			protected Project getProject() {
				return null;
			}

			@Override
			protected TimesheetSetting getSetting() {
				return TimesheetWidgetPanel.this.getSetting();
			}

			@Override
			protected LocalDate getBaseDate() {
				return baseDate;
			}
		});
	}

	protected abstract TimesheetSetting getSetting();
	
}
