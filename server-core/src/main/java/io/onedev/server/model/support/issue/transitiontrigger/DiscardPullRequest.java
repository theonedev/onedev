package io.onedev.server.model.support.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=300, name="Pull request fixing the issue is discarded")
public class DiscardPullRequest extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

}
