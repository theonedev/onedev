package io.onedev.server.manager;

import io.onedev.server.model.PullRequestLabel;

public interface PullRequestLabelManager extends EntityLabelManager<PullRequestLabel> {

	void create(PullRequestLabel pullRequestLabel);
	
}
