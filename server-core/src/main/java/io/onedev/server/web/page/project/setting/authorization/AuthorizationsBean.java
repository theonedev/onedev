package io.onedev.server.web.page.project.setting.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;

@Editable
public class AuthorizationsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<AuthorizationBean> authorizations = new ArrayList<>();

	@Editable
	public List<AuthorizationBean> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<AuthorizationBean> authorizations) {
		this.authorizations = authorizations;
	}
	
}
