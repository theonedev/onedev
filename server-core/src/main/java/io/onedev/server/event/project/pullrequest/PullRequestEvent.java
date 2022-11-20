package io.onedev.server.event.project.pullrequest;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestManager;
import io.onedev.server.entitymanager.UrlManager;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long requestId;
	
	public PullRequestEvent(User user, Date date, PullRequest request) {
		super(user, date, request.getTargetProject());
		requestId = request.getId();
	}

	public PullRequest getRequest() {
		return OneDev.getInstance(PullRequestManager.class).load(requestId);
	}

	@Override
	public String getLockName() {
		return PullRequest.getSerialLockName(requestId);
	}
	
	@Override
	public String getUrl() {
		return OneDev.getInstance(UrlManager.class).urlFor(getRequest());
	}
	
}
