package io.onedev.server.model.support.issueworkflow.action;

import java.io.Serializable;

import javax.annotation.Nullable;

import io.onedev.server.util.editable.annotation.Editable;

@Editable
public interface IssueAction extends Serializable {

	@Nullable
	Button getButton();
	
}
