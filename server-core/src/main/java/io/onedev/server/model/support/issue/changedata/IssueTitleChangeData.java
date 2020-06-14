package io.onedev.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;

import io.onedev.server.model.Group;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.propertychangepanel.PropertyChangePanel;

public class IssueTitleChangeData implements IssueChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public IssueTitleChangeData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public Component render(String componentId, IssueChange change) {
		return new PropertyChangePanel(componentId, 
				CollectionUtils.newHashMap("Title", oldTitle), 
				CollectionUtils.newHashMap("Title", newTitle), 
				true);
	}
	
	@Override
	public String getActivity(Issue withIssue) {
		String activity = "changed title";
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
		return false;
	}
	
}
