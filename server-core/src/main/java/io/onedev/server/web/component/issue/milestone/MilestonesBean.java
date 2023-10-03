package io.onedev.server.web.component.issue.milestone;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.MilestoneChoice;

import java.io.Serializable;
import java.util.List;

@Editable
public class MilestonesBean implements Serializable {
	
	private List<String> milestoneNames;

	@Editable
	@MilestoneChoice
	public List<String> getMilestoneNames() {
		return milestoneNames;
	}

	public void setMilestoneNames(List<String> milestoneNames) {
		this.milestoneNames = milestoneNames;
	}

}
