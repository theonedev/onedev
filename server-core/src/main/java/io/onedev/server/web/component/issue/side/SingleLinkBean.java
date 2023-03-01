package io.onedev.server.web.component.issue.side;

import java.io.Serializable;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.IssueChoice;

@Editable
public class SingleLinkBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long issueId;

	@IssueChoice
	@Editable
	public Long getIssueId() {
		return issueId;
	}

	public void setIssueId(Long issueId) {
		this.issueId = issueId;
	}
	
}
