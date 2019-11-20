package io.onedev.server.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200, name="Pull request fixing the issue is opened")
public class OpenPullRequest extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

}
