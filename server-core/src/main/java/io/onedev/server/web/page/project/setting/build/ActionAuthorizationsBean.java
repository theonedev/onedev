package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ActionAuthorizationsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ActionAuthorization> actionAuthorizations = new ArrayList<>();

	@Editable
	public List<ActionAuthorization> getActionAuthorizations() {
		return actionAuthorizations;
	}

	public void setActionAuthorizations(List<ActionAuthorization> actionAuthorizations) {
		this.actionAuthorizations = actionAuthorizations;
	}
	
}
