package io.onedev.server.model.support.issue.workflow.transitiontrigger;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface TransitionTrigger extends Serializable {

	@Nullable
	Button getButton();
	
}
