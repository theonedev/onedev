package io.onedev.server.web.component.issue.progress;

import io.onedev.server.OneDev;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.WorkingPeriod;
import io.onedev.server.entitymanager.SettingManager;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable(name="Edit Estimated Time")
public class EstimatedTimeEditBean implements Serializable {
	
	private Integer estimatedTime;

	@Editable(descriptionProvider="getEstimatedTimeDescription")
	@OmitName
	@WorkingPeriod
	@NotNull(message = "Must not be empty")
	@Min(1)
	public Integer getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(Integer estimatedTime) {
		this.estimatedTime = estimatedTime;
	}
	
	private static String getEstimatedTimeDescription() {
		var aggregationLink = OneDev.getInstance(SettingManager.class).getIssueSetting().getTimeTrackingSetting().getAggregationLink();
		if (aggregationLink != null)
			return "Specify estimated time <b class='text-warning'>only for this issue</b>, not counting '" + aggregationLink + "'";
		else 
			return "";
	}
}
