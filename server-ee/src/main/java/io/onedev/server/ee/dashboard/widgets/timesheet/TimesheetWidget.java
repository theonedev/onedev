package io.onedev.server.ee.dashboard.widgets.timesheet;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.model.support.Widget;
import io.onedev.server.model.support.issue.TimesheetSetting;
import org.apache.wicket.Component;

import javax.validation.constraints.NotNull;

@Editable(name="Timesheet", order=500)
public class TimesheetWidget extends Widget {

	private static final long serialVersionUID = 1L;

	private TimesheetSetting setting = new TimesheetSetting();

	@Editable(order=100)
	@OmitName
	@NotNull
	public TimesheetSetting getSetting() {
		return setting;
	}

	public void setSetting(TimesheetSetting setting) {
		this.setting = setting;
	}
	
	@Override
	protected Component doRender(String componentId) {
		return new TimesheetWidgetPanel(componentId) {
			@Override
			protected String getTitle() {
				return TimesheetWidget.this.getTitle();
			}

			@Override
			protected TimesheetSetting getSetting() {
				return setting;
			}
		};
	}

}
