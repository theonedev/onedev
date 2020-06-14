package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.util.CommentAware;
import io.onedev.server.web.component.propertychangepanel.PropertyChangePanel;

public class PullRequestMergeStrategyChangeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final MergeStrategy oldStrategy;
	
	private final MergeStrategy newStrategy;
	
	public PullRequestMergeStrategyChangeData(MergeStrategy oldStrategy, MergeStrategy newStrategy) {
		this.oldStrategy = oldStrategy;
		this.newStrategy = newStrategy;
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "changed merge strategy";
		if (withRequest != null)
			activity += " of pull request" + withRequest.getNumberAndTitle();
		return activity;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return new PropertyChangePanel(componentId, 
				CollectionUtils.newHashMap("Merge Strategy", oldStrategy.toString()), 
				CollectionUtils.newHashMap("Merge Strategy", newStrategy.toString()), 
				true);
	}
	
	@Override
	public CommentAware getCommentAware() {
		return null;
	}

}
