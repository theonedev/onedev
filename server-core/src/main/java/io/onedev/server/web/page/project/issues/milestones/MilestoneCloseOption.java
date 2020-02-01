package io.onedev.server.web.page.project.issues.milestones;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.model.Milestone;
import io.onedev.server.util.EditContext;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.ShowCondition;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.util.WicketUtils;

@Editable
public class MilestoneCloseOption implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean moveTodoIssuesToAnotherMilestone = true;
	
	private String milestoneToClose;
	
	private String newMilestone;

	@Editable(order=100)
	public String getMilestoneToClose() {
		return milestoneToClose;
	}

	public void setMilestoneToClose(String milestoneToClose) {
		this.milestoneToClose = milestoneToClose;
	}

	@Editable
	public boolean isMoveTodoIssuesToAnotherMilestone() {
		return moveTodoIssuesToAnotherMilestone;
	}

	public void setMoveTodoIssuesToAnotherMilestone(boolean moveTodoIssuesToAnotherMilestone) {
		this.moveTodoIssuesToAnotherMilestone = moveTodoIssuesToAnotherMilestone;
	}

	@Editable(order=200, name="Another Open Milestone")
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
		return (Boolean) EditContext.get().getInputValue("moveTodoIssuesToAnotherMilestone");
	}
	
	@SuppressWarnings("unused")
	private static List<String> getNewMilestoneChoices() {
		List<String> choices = new ArrayList<>();
		ProjectPage page = (ProjectPage) WicketUtils.getPage();
		Object milestoneToDelete = EditContext.get().getInputValue("milestoneToClose");
		for (Milestone milestone: page.getProject().getMilestones()) {
			if (!milestone.isClosed() && !milestone.getName().equals(milestoneToDelete))
				choices.add(milestone.getName());
		}
		return choices;
	}
}
