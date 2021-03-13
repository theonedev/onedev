package io.onedev.server.web.page.project.pullrequests.detail;

import java.util.List;

import io.onedev.commons.launcher.loader.ExtensionPoint;
import io.onedev.server.model.PullRequest;

@ExtensionPoint
public interface PullRequestSummaryContribution {

	List<PullRequestSummaryPart> getParts(PullRequest request);
	
	int getOrder();
	
}
