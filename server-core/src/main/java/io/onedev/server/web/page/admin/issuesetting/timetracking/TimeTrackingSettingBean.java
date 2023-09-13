package io.onedev.server.web.page.admin.issuesetting.timetracking;

import io.onedev.server.annotation.Editable;
import io.onedev.server.model.support.issue.TimeTrackingSetting;

import javax.annotation.Nullable;
import java.io.Serializable;

@Editable
public class TimeTrackingSettingBean implements Serializable {
	
	private TimeTrackingSetting timeTrackingSetting;

	@Editable(name="Enable")
	@Nullable
	public TimeTrackingSetting getTimeTrackingSetting() {
		return timeTrackingSetting;
	}

	public void setTimeTrackingSetting(TimeTrackingSetting timeTrackingSetting) {
		this.timeTrackingSetting = timeTrackingSetting;
	}
}
