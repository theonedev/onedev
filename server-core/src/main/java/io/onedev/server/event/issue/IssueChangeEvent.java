package io.onedev.server.event.issue;

import java.util.Collection;
import java.util.Map;

import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;

public class IssueChangeEvent extends IssueEvent {

	private final IssueChange change;
	
	public IssueChangeEvent(IssueChange change) {
		super(change.getUser(), change.getDate(), change.getIssue());
		this.change = change;
	}

	public IssueChange getChange() {
		return change;
	}

	@Override
	public String getMarkdown() {
		if (change.getData().getCommentAware() != null)
			return change.getData().getCommentAware().getComment();
		else
			return null;
	}

	@Override
	public boolean affectsBoards() {
		return change.affectsBoards();
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return change.getData().getNewUsers();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return change.getData().getNewGroups();
	}

	@Override
	public String getActivity() {
		return getChange().getData().getActivity();
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return getChange().getData().getActivityDetail();
	}

}
