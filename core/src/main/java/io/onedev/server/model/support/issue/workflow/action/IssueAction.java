package io.onedev.server.model.support.issue.workflow.action;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public interface IssueAction extends Serializable {

	@Nullable
	Button getButton();
	
}
