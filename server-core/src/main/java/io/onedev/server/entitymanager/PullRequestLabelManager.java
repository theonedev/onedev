package io.onedev.server.entitymanager;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;

import java.util.Collection;

public interface PullRequestLabelManager extends EntityLabelManager<PullRequestLabel> {

	void create(PullRequestLabel pullRequestLabel);
	
	void populateLabels(Collection<PullRequest> pullRequests);
	
}
