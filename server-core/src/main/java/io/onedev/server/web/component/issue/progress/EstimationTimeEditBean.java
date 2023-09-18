package io.onedev.server.web.component.issue.progress;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.OmitName;
import io.onedev.server.annotation.WorkingPeriod;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Editable(name="Edit Estimated Time")
public class EstimationTimeEditBean implements Serializable {
	
	private Integer estimationTime;

	@Editable
	@OmitName
	@NotNull(message="May not be empty")
	@WorkingPeriod
	@Min(1)
	public Integer getEstimationTime() {
		return estimationTime;
	}

	public void setEstimationTime(Integer estimationTime) {
		this.estimationTime = estimationTime;
	}
}
