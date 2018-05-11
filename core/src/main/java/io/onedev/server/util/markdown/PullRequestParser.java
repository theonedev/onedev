package io.onedev.server.util.markdown;

import io.onedev.server.OneDev;
import io.onedev.server.manager.PullRequestManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;

public class PullRequestParser extends ReferenceParser<PullRequest> {

	@Override
	protected PullRequest findReferenceable(Project project, long number) {
		return OneDev.getInstance(PullRequestManager.class).find(project, number);
	}
	
}
