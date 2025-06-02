package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;

public class PullRequestTouched extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long requestId;
	
	public PullRequestTouched(Project project, Long requestId) {
		super(SecurityUtils.getUser(), new Date(), project);
		this.requestId = requestId;
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public String getActivity() {
		return "touched";
	}
	
}
