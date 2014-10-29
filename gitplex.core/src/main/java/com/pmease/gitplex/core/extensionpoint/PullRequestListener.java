package com.pmease.gitplex.core.extensionpoint;

import com.pmease.commons.loader.ExtensionPoint;
import com.pmease.gitplex.core.model.PullRequest;

@ExtensionPoint
public interface PullRequestListener {
	void onOpened(PullRequest request);
	
	void onUpdated(PullRequest request);
	
	void onCommented(PullRequest request);
	
	void onVoted(PullRequest request);
	
	void onIntegrated(PullRequest request);
	
	void onDiscarded(PullRequest request);

	void onIntegrationPreviewCalculated(PullRequest request);
}
