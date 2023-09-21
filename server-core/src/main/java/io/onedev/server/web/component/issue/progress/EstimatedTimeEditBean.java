package io.onedev.server.web.component.issue.progress;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.WorkingPeriod;

import javax.validation.constraints.Min;
import java.io.Serializable;

@Editable(name="Edit Estimated Time")
public class EstimatedTimeEditBean implements Serializable {
	
	private int estimatedTime;

	@Editable
	@OmitName
	@WorkingPeriod
	@Min(1)
	public int getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(int estimatedTime) {
		this.estimatedTime = estimatedTime;
	}
}
