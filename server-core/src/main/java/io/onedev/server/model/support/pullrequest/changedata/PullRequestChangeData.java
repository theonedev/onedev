package io.onedev.server.model.support.pullrequest.changedata;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;

public interface PullRequestChangeData extends Serializable {

	abstract Component render(String componentId, PullRequestChange change);
	
	abstract String getActivity(@Nullable PullRequest withRequest);
	
	@Nullable
	abstract CommentAware getCommentAware();

}
