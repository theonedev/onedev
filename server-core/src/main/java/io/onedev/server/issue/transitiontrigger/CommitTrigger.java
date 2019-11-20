package io.onedev.server.issue.transitiontrigger;

import io.onedev.server.web.editable.annotation.Editable;

@Editable(order=500, name="Issue is committed")
public class CommitTrigger extends TransitionTrigger {

	private static final long serialVersionUID = 1L;

}
