package io.onedev.server.event.project.issue;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.security.SecurityUtils;

import java.util.Date;

public class IssueDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long issueId;
	
	public IssueDeleted(Issue issue) {
		super(SecurityUtils.getUser(), new Date(), issue.getProject());
		issueId = issue.getId();
	}

	public Long getIssueId() {
		return issueId;
	}

	@Override
	public String getActivity() {
		return "issue deleted";
	}
	
}
