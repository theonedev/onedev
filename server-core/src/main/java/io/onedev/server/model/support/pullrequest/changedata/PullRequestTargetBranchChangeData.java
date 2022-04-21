package io.onedev.server.model.support.pullrequest.changedata;

import java.util.HashMap;
import java.util.Map;

import io.onedev.server.notification.ActivityDetail;

public class PullRequestTargetBranchChangeData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String oldBranch;
	
	private final String newBranch;
	
	public PullRequestTargetBranchChangeData(String oldBranch, String newBranch) {
		this.oldBranch = oldBranch;
		this.newBranch = newBranch;
	}
	
	@Override
	public String getActivity() {
		return "changed target branch";
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldProperties = new HashMap<>();
		oldProperties.put("Target Branch", oldBranch);
		Map<String, String> newProperties = new HashMap<>();
		newProperties.put("Target Branch", newBranch);
		
		return ActivityDetail.compare(oldProperties, newProperties, true);
	}
	
}