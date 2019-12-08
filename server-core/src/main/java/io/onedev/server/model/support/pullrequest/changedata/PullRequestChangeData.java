package io.onedev.server.model.support.pullrequest.changedata;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;

public interface PullRequestChangeData extends Serializable {

	abstract Component render(String componentId, PullRequestChange change);
	
	abstract String getDescription();
	
	@Nullable
	abstract CommentSupport getCommentSupport();

}
