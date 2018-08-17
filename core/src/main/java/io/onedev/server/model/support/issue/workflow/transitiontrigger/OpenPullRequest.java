package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=200, name="Pull request is opened")
public class OpenPullRequest extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

}
