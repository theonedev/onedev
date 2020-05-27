package io.onedev.server.event.issue;

import java.util.Collection;
import java.util.Map;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.Group;
import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;

public class IssueChangeEvent extends IssueEvent implements MarkdownAware {

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
	public String getActivity(boolean withEntity) {
		return getChange().getData().getActivity(withEntity?getChange().getIssue():null);
	}

}
