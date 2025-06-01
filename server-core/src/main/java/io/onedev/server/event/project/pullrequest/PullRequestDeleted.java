package io.onedev.server.event.project.pullrequest;

import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.security.SecurityUtils;

import static io.onedev.server.web.translation.Translation._T;

import java.util.Date;

public class PullRequestDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long requestId;
	
	public PullRequestDeleted(PullRequest request) {
		super(SecurityUtils.getUser(), new Date(), request.getProject());
		requestId = request.getId();
	}

	public Long getRequestId() {
		return requestId;
	}

	@Override
	public String getActivity() {
		return _T("pull request deleted");
	}
	
}
