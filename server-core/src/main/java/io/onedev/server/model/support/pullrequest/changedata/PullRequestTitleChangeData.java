package io.onedev.server.model.support.pullrequest.changedata;

import java.util.HashMap;
import java.util.Map;

import io.onedev.server.notification.ActivityDetail;
import io.onedev.server.util.CommentAware;

public class PullRequestTitleChangeData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldTitle;
	
	private final String newTitle;
	
	public PullRequestTitleChangeData(String oldTitle, String newTitle) {
		this.oldTitle = oldTitle;
		this.newTitle = newTitle;
	}
	
	@Override
	public String getActivity() {
		return "changed title";
	}

	@Override
	public CommentAware getCommentAware() {
		return null;
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldProperties = new HashMap<>();
		oldProperties.put("Title", oldTitle);
		Map<String, String> newProperties = new HashMap<>();
		oldProperties.put("Title", newTitle);
		
		return ActivityDetail.compare(oldProperties, newProperties, true);
	}
	
}