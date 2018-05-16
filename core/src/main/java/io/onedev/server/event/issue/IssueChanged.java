package io.onedev.server.event.issue;

import java.util.Date;

import io.onedev.server.model.IssueChange;
import io.onedev.server.model.User;
import io.onedev.server.util.editable.annotation.Editable;

@Editable(name="changed")
public class IssueChanged extends IssueEvent {

	private final IssueChange change;
	
	public IssueChanged(IssueChange change) {
		super(change.getIssue());
		this.change = change;
	}

	public IssueChange getChange() {
		return change;
	}

	@Override
	public User getUser() {
		return change.getUser();
	}

	@Override
	public Date getDate() {
		return change.getDate();
	}

}
