package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.Milestone;
import io.onedev.server.model.User;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.propertychangepanel.PropertyChangePanel;

public class IssueMilestoneChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldMilestone;
	
	private final String newMilestone;
	
	public IssueMilestoneChangeData(@Nullable Milestone oldMilestone, @Nullable Milestone newMilestone) {
		this.oldMilestone = oldMilestone!=null?oldMilestone.getName():null;
		this.newMilestone = newMilestone!=null?newMilestone.getName():null;
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
		return new PropertyChangePanel(componentId, 
				CollectionUtils.newHashMap("Milestone", oldMilestone), 
				CollectionUtils.newHashMap("Milestone", newMilestone), 
				true);
	}
	
	public String getOldMilestone() {
		return oldMilestone;
	}

	public String getNewMilestone() {
		return newMilestone;
	}

	@Override
	public String getActivity(Issue withIssue) {
		String activity = "changed milestone";
		if (withIssue != null)
			activity += " of issue " + withIssue.getNumberAndTitle();
		return activity;
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

	@Override
	public boolean affectsBoards() {
		return true;
	}
	
}
