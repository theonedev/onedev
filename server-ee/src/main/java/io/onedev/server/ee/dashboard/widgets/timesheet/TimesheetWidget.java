package io.onedev.server.ee.dashboard.widgets.timesheet;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueQuery;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.ProjectChoice;
import io.onedev.server.ee.dashboard.widgets.timesheet.TimesheetWidgetPanel;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.Widget;
import io.onedev.server.model.support.issue.TimesheetSetting;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessProject;
import io.onedev.server.web.component.issue.list.IssueListPanel;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	public int getDefaultWidth() {
		return 10;
	}

	@Override
	public int getDefaultHeight() {
		return 8;
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
