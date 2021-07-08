package io.onedev.server.event.issue;

import java.util.Collection;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.IssueComment;

public class IssueCommented extends IssueEvent implements MarkdownAware {

	private final IssueComment comment;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public IssueCommented(IssueComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getIssue());
		this.comment = comment;
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public IssueComment getComment() {
		return comment;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public boolean affectsBoards() {
		return false;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "commented";
		if (withEntity)
			activity += " on issue " + comment.getIssue().getNumberAndTitle();
		return activity;
	}

}
