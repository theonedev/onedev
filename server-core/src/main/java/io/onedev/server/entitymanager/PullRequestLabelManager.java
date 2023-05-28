package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequestLabel;

public interface PullRequestLabelManager extends EntityLabelManager<PullRequestLabel> {

	void create(PullRequestLabel pullRequestLabel);
	
}
