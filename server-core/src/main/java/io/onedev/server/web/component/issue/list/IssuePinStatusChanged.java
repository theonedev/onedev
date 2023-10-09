package io.onedev.server.web.component.issue.list;

import io.onedev.server.web.util.AjaxPayload;
import org.apache.wicket.ajax.AjaxRequestTarget;

public class IssuePinStatusChanged extends AjaxPayload {

	private final Long issueId;
	
	public IssuePinStatusChanged(AjaxRequestTarget target, Long issueId) {
		super(target);
		this.issueId = issueId;
	}

	public Long getIssueId() {
		return issueId;
	}
}