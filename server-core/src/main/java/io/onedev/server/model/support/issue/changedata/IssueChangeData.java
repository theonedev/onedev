package io.onedev.server.model.support.issue.changedata;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;

public abstract class IssueChangeData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract String getActivity();

	@Nullable
	public abstract CommentAware getCommentAware();
	
	public abstract Map<String, Collection<User>> getNewUsers();
	
	public abstract Map<String, Group> getNewGroups();
	
	public abstract boolean affectsListing();

	@Nullable
	public ActivityDetail getActivityDetail() {
		return null;
	}
	
}
