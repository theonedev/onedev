package io.onedev.server.web.page.project.issues.issuedetail.activities;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.model.IssueComment;
import io.onedev.server.web.util.AjaxPayload;

public class IssueCommentDeleted extends AjaxPayload {

	private final IssueComment comment;
	
	public IssueCommentDeleted(IPartialPageRequestHandler handler, IssueComment comment) {
		super(handler);
		this.comment = comment;
	}

	public IssueComment getComment() {
		return comment;
	}

}
