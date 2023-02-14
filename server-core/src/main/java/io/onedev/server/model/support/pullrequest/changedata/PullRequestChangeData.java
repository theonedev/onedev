package io.onedev.server.model.support.pullrequest.changedata;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.notification.ActivityDetail;

public abstract class PullRequestChangeData implements Serializable {

	private static final long serialVersionUID = 1L;

	public abstract String getActivity();

	public boolean isMinor() {
		return false;
	}
	
	@Nullable
	public ActivityDetail getActivityDetail() {
		return null;
	}
	
}
