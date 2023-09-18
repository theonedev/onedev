package io.onedev.server.web.component.issue.progress;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Multiline;
import io.onedev.server.annotation.WorkingPeriod;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Editable(name="Log Work")
public class IssueWorkEditBean implements Serializable {
	
	private Integer spentTime;
	
	private Date startAt = new Date();
	
	private String note;

	@Editable(order=100, name="Add Spent Time")
	@WorkingPeriod
	@NotNull(message="May not be empty")
	@Min(1)
	public Integer getSpentTime() {
		return spentTime;
	}

	public void setSpentTime(Integer spentTime) {
		this.spentTime = spentTime;
	}
	
	@Editable(order=200, description = "When this work starts")
	@NotNull
	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	@Editable(order=300, description = "Optionally leave a note")
	@Multiline
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
}
