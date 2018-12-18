package io.onedev.server.event.issue;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueChange;

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
		if (change.getData().getCommentSupport() != null)
			return change.getData().getCommentSupport().getComment();
		else
			return null;
	}

	@Override
	public boolean affectsBoards() {
		return change.affectsBoards();
	}

}
