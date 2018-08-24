package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name="Issue is committed")
public class CommitTrigger implements TransitionTrigger {

	private static final long serialVersionUID = 1L;

}
