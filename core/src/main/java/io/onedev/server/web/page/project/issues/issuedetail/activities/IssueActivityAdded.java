package io.onedev.server.web.page.project.issues.issuedetail.activities;

import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;

import io.onedev.server.web.page.project.issues.issuedetail.activities.activity.IssueActivity;
import io.onedev.server.web.util.AjaxPayload;

public class IssueActivityAdded extends AjaxPayload {

	private final IssueActivity activity;
	
	public IssueActivityAdded(IPartialPageRequestHandler handler, IssueActivity activity) {
		super(handler);
		this.activity = activity;
	}

	public IssueActivity getActivity() {
		return activity;
	}

}
