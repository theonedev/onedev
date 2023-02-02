package io.onedev.server.event.project.issue;

import io.onedev.server.OneDev;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class IssuesMoved extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long sourceProjectId;
	
	private final Collection<Long> issueIds;
	
	public IssuesMoved(Project sourceProject, Project targetProject, Collection<Issue> issues) {
		super(SecurityUtils.getUser(), new Date(), targetProject);
		sourceProjectId = sourceProject.getId();
		issueIds = issues.stream().map(Issue::getId).collect(Collectors.toSet());
	}

	public Project getSourceProject() {
		return OneDev.getInstance(Dao.class).load(Project.class, sourceProjectId);
	}

	public Project getTargetProject() {
		return getProject();
	}
	
	public Collection<Long> getIssueIds() {
		return issueIds;
	}

	@Override
	public String getActivity() {
		return "issues moved";
	}
	
}
