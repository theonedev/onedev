package io.onedev.server.event.pullrequest;

import java.util.Collection;

import io.onedev.server.model.PullRequestComment;

public class PullRequestCommented extends PullRequestEvent {

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
	public String getActivity() {
		return "commented";
	}

}
