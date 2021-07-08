package io.onedev.server.event.pullrequest;

import java.util.Collection;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestComment;

public class PullRequestCommented extends PullRequestEvent implements MarkdownAware {

	private final PullRequestComment comment;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public PullRequestCommented(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getRequest());
		this.comment = comment;
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public PullRequestComment getComment() {
		return comment;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	public String getMarkdown() {
		return getComment().getContent();
	}

	@Override
	public String getActivity(boolean withEntity) {
		String activity = "commented";
		if (withEntity)
			activity += " on pull request " + getRequest().getNumberAndTitle();
		return activity;
	}

}
