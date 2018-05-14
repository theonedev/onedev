package io.onedev.server.web.page.project.issues.issuedetail.activities.activity;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.onedev.server.web.util.AjaxPayload;

public class IssueCommentDeleted extends AjaxPayload {

	public IssueCommentDeleted(AjaxRequestTarget target) {
		super(target);
	}

}
