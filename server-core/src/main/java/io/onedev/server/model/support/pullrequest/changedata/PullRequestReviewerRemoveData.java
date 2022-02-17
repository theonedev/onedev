package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.User;

public class PullRequestReviewerRemoveData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final Long reviewerId;
	
	public PullRequestReviewerRemoveData(User reviewer) {
		this.reviewerId = reviewer.getId();
	}
	
	@Override
	public String getActivity() {
		User user = getUser();
		if (user != null)
			return "removed reviewer \"" + user.getDisplayName() + "\"";
		else
			return "removed reviewer unknown";
	}

	@Nullable
	public User getUser() {
		return OneDev.getInstance(UserManager.class).get(reviewerId);
	}
	
}
