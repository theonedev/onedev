package io.onedev.server.web.page.project.issues.timesheets;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.issue.TimesheetSetting;

import javax.validation.constraints.NotEmpty;

@Editable(name="Edit Timesheet")
public class TimesheetEditBean extends TimesheetSetting {

	private static final long serialVersionUID = 1L;
	
	private String name;
	
	@Editable(order=10)
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
