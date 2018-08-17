package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=250, name="Pull request is merged")
public class MergePullRequest extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

}
