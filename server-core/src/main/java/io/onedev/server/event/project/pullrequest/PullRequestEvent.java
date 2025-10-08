package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.web.UrlService;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long requestId;
	
	public PullRequestEvent(@Nullable User user, Date date, PullRequest request) {
		super(user, date, request.getTargetProject());
		requestId = request.getId();
	}

	public PullRequest getRequest() {
		return OneDev.getInstance(PullRequestService.class).load(requestId);
	}

	@Override
	public String getLockName() {
		return PullRequest.getSerialLockName(requestId);
	}
	
	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlService.class).urlFor(getRequest(), true);
	}
	
}
