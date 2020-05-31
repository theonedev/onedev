package io.onedev.server.web.page.project.setting.build;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class ActionAuthorizationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private ActionAuthorization authorization;

	@Editable(name="Action")
	@NotNull
	public ActionAuthorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(ActionAuthorization authorization) {
		this.authorization = authorization;
	}
	
}
