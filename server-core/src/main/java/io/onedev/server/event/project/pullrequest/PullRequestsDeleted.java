package io.onedev.server.event.project.pullrequest;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class PullRequestsDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Collection<Long> requestIds;
	
	public PullRequestsDeleted(Project project, Collection<PullRequest> requests) {
		super(SecurityUtils.getUser(), new Date(), project);
		requestIds = requests.stream().map(PullRequest::getId).collect(Collectors.toSet());
	}

	public Collection<Long> getRequestIds() {
		return requestIds;
	}

	@Override
	public String getActivity() {
		return "pull requests deleted";
	}
	
}
