package io.onedev.server.web.page.project.issues.milestones;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Milestone;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class MilestoneDeleteOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean moveIssuesToAnotherMilestone = true;
	
	private String milestoneToDelete;
	
	private String newMilestone;

	@Editable
	public String getMilestoneToDelete() {
		return milestoneToDelete;
	}

	public void setMilestoneToDelete(String milestoneToDelete) {
		this.milestoneToDelete = milestoneToDelete;
	}

	@Editable(order=100)
	public boolean isMoveIssuesToAnotherMilestone() {
		return moveIssuesToAnotherMilestone;
	}

	public void setMoveIssuesToAnotherMilestone(boolean moveIssuesToAnotherMilestone) {
		this.moveIssuesToAnotherMilestone = moveIssuesToAnotherMilestone;
	}

	@Editable(order=200, name="Another Milestone")
	@ShowCondition("isNewMilestoneVisible")
	@ChoiceProvider("getNewMilestoneChoices")
	@OmitName
	@NotEmpty
	public String getNewMilestone() {
		return newMilestone;
	}

	public void setNewMilestone(String newMilestone) {
		this.newMilestone = newMilestone;
	}
	
	@SuppressWarnings("unused")
	private static boolean isNewMilestoneVisible() {
		return (Boolean) OneContext.get().getEditContext().getInputValue("moveIssuesToAnotherMilestone");
	}
	
	@SuppressWarnings("unused")
	private static List<String> getNewMilestoneChoices() {
		List<String> choices = new ArrayList<>();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		Object milestoneToDelete = OneContext.get().getEditContext().getInputValue("milestoneToDelete");
		for (Milestone milestone: page.getProject().getMilestones()) {
			if (!milestone.getName().equals(milestoneToDelete))
				choices.add(milestone.getName());
		}
		return choices;
	}
}
