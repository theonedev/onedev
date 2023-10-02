package io.onedev.server.ee.timetracking;

import io.onedev.server.model.support.issue.TimesheetSetting;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;

import javax.annotation.Nullable;
import java.time.LocalDate;

import static io.onedev.server.model.support.issue.TimesheetSetting.DateRangeType.WEEK;

public abstract class DateRangeNavigator extends Panel {
	
	public DateRangeNavigator(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new AjaxLink<Void>("prevMonthOrWeek") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				var baseDate = getBaseDate();
				if (baseDate == null)
					baseDate = LocalDate.now();
				if (getTimesheetSetting().getDateRangeType() == WEEK)
					baseDate = baseDate.minusWeeks(1);
				else
					baseDate = baseDate.minusMonths(1);
				onBaseDateUpdate(target, baseDate);
			}
		});
		add(new AjaxLink<Void>("thisMonthOrWeek") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {
					@Override
					public String getObject() {
						if (getTimesheetSetting().getDateRangeType() == WEEK)
							return "This Week";
						else
							return "This Month";
					}
				}));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onBaseDateUpdate(target, null);
			}
		});
		add(new AjaxLink<Void>("nextMonthOrWeek") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				var baseDate = getBaseDate();
				if (baseDate == null)
					baseDate = LocalDate.now();
				if (getTimesheetSetting().getDateRangeType() == WEEK)
					baseDate = baseDate.plusWeeks(1);
				else
					baseDate = baseDate.plusMonths(1);
				onBaseDateUpdate(target, baseDate);
			}
		});
	}
	
	@Nullable
	protected abstract LocalDate getBaseDate();
	
	protected abstract void onBaseDateUpdate(AjaxRequestTarget target, LocalDate baseDate);
	
	protected abstract TimesheetSetting getTimesheetSetting();
	
}
