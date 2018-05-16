package io.onedev.server.web.page.project.issues.issuedetail.activities;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.model.IssueVote;
import io.onedev.server.web.util.AjaxPayload;

public class IssueVoteDeleted extends AjaxPayload {

	private final IssueVote vote;
	
	public IssueVoteDeleted(IPartialPageRequestHandler handler, IssueVote vote) {
		super(handler);
		this.vote = vote;
	}

	public IssueVote getVote() {
		return vote;
	}

}
