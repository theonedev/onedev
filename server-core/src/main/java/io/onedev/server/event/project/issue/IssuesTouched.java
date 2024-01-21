package io.onedev.server.event.project.issue;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

import java.util.Collection;
import java.util.Date;

public class IssuesTouched extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Collection<Long> issueIds;
	
	public IssuesTouched(Project project, Collection<Long> issueIds) {
		super(SecurityUtils.getUser(), new Date(), project);
		this.issueIds = issueIds;
	}
	
	public Collection<Long> getIssueIds() {
		return issueIds;
	}

	@Override
	public String getActivity() {
		return "issues touched";
	}
	
}
