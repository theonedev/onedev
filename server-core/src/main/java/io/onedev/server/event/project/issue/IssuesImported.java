package io.onedev.server.event.project.issue;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class IssuesImported extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Collection<Long> issueIds;
	
	public IssuesImported(Project project, Collection<Issue> issues) {
		super(SecurityUtils.getUser(), new Date(), project);
		issueIds = issues.stream().map(Issue::getId).collect(Collectors.toSet());
	}
	
	public Collection<Long> getIssueIds() {
		return issueIds;
	}

	@Override
	public String getActivity() {
		return "issues imported";
	}
	
}
