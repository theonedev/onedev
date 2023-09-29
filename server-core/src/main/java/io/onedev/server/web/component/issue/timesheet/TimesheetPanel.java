package io.onedev.server.web.component.issue.timesheet;

import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.TimesheetSetting;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static io.onedev.server.model.support.issue.TimesheetSetting.TimeRangeType.WEEK;

public abstract class TimesheetPanel extends Panel {
	
	public TimesheetPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("fromDate", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return getFromDate().toString();
			}
		}));
		add(new Label("toDate", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return getToDate().toString();
			}
		}));
	}

	protected abstract Project getProject();
	
	protected abstract TimesheetSetting getSetting();
	
	protected abstract LocalDate getBaseDate();

	private LocalDate getFromDate() {
		var fromDate = getBaseDate();
		if (fromDate == null)
			fromDate = LocalDate.now();
		if (getSetting().getTimeRangeType() == WEEK)
			fromDate = fromDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		else
			fromDate = fromDate.with(TemporalAdjusters.firstDayOfMonth());
		return fromDate;
	}

	private LocalDate getToDate() {
		var toDate = getBaseDate();
		if (toDate == null)
			toDate = LocalDate.now();
		if (getSetting().getTimeRangeType() == WEEK)
			toDate = toDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		else
			toDate = toDate.with(TemporalAdjusters.lastDayOfMonth());
		return toDate;
	}
	
}
