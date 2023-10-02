package io.onedev.server.ee.dashboard.widgets.timesheet;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.ee.timetracking.DateRangeNavigator;
import io.onedev.server.ee.timetracking.TimesheetPanel;
import io.onedev.server.ee.timetracking.TimesheetXlsxResource;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.Panel;

import java.time.LocalDate;

public abstract class TimesheetWidgetPanel extends Panel {
	
	private LocalDate baseDate;
	
	private TimesheetPanel timesheet;
	
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
				target.add(timesheet);
			}

			@Override
			protected TimesheetSetting getTimesheetSetting() {
				return getSetting();
			}
			
		});

		add(new ResourceLink<Void>("export", new TimesheetXlsxResource() {

			@Override
			protected String getTitle() {
				return TimesheetWidgetPanel.this.getTitle();
			}

			@Override
			protected TimesheetPanel getTimesheet() {
				return timesheet;
			}
		}));
		
		add(timesheet = new TimesheetPanel("body") {
			
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

	protected abstract String getTitle();
	
	protected abstract TimesheetSetting getSetting();
	
}
