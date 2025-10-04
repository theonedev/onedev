package io.onedev.server.service;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;

import java.util.Collection;

public interface PullRequestLabelService extends EntityLabelService<PullRequestLabel> {

	void create(PullRequestLabel pullRequestLabel);
	
	void populateLabels(Collection<PullRequest> pullRequests);
	
}
