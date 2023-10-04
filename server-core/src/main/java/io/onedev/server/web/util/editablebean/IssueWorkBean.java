package io.onedev.server.web.util.editablebean;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Markdown;
import io.onedev.server.annotation.WithTime;
import io.onedev.server.annotation.WorkingPeriod;
import io.onedev.server.entitymanager.SettingManager;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Editable(name="Log Work")
public class IssueWorkBean implements Serializable {
	
	private Integer spentTime = 8 * 60;
	
	private Date startAt = new Date();
	
	private String note;

	@Editable(order=100, name="Add Spent Time", descriptionProvider = "getSpentTimeDescription")
	@WorkingPeriod
	@NotNull(message = "Must not be empty")
	@Min(1)
	public Integer getSpentTime() {
		return spentTime;
	}

	public void setSpentTime(Integer spentTime) {
		this.spentTime = spentTime;
	}

	private static String getSpentTimeDescription() {
		var aggregationLink = OneDev.getInstance(SettingManager.class).getIssueSetting().getTimeTrackingSetting().getAggregationLink();
		if (aggregationLink != null)
			return "Add spent time <b class='text-warning'>only for this issue</b>, not counting '" + aggregationLink + "'";
		else
			return "";
	}
	
	@Editable(order=200, description = "When this work starts")
	@WithTime
	@NotNull
	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	@Editable(order=300, description = "Optionally leave a note")
	@Markdown
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
}
