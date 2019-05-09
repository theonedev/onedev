package io.onedev.server.event.pullrequest;

import java.util.Collection;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Sets;

import io.onedev.server.event.BuildCommitsAware;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;

public abstract class PullRequestEvent extends ProjectEvent implements BuildCommitsAware {

	private final PullRequest request;
	
	public PullRequestEvent(User user, Date date, PullRequest request) {
		super(user, date, request.getTargetProject());
		this.request = request;
	}

	public PullRequest getRequest() {
		return request;
	}

	@Override
	public Collection<ObjectId> getBuildCommits() {
		return Sets.newHashSet(request.getHeadCommit().copy());
	}
	
}
