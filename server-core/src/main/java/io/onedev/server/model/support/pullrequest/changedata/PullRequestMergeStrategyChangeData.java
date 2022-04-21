package io.onedev.server.model.support.pullrequest.changedata;

import java.util.HashMap;
import java.util.Map;

import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.notification.ActivityDetail;

public class PullRequestMergeStrategyChangeData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final MergeStrategy oldStrategy;
	
	private final MergeStrategy newStrategy;
	
	public PullRequestMergeStrategyChangeData(MergeStrategy oldStrategy, MergeStrategy newStrategy) {
		this.oldStrategy = oldStrategy;
		this.newStrategy = newStrategy;
	}
	
	@Override
	public String getActivity() {
		return "changed merge strategy";
	}

	@Override
	public ActivityDetail getActivityDetail() {
		Map<String, String> oldProperties = new HashMap<>();
		oldProperties.put("Merge Strategy", oldStrategy.name());
		Map<String, String> newProperties = new HashMap<>();
		newProperties.put("Merge Strategy", newStrategy.name());
		
		return ActivityDetail.compare(oldProperties, newProperties, true);
	}
	
}
