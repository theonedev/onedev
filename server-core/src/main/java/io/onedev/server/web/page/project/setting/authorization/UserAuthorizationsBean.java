package io.onedev.server.web.page.project.setting.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable
public class UserAuthorizationsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<UserAuthorizationBean> authorizations = new ArrayList<>();

	@Editable
	public List<UserAuthorizationBean> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<UserAuthorizationBean> authorizations) {
		this.authorizations = authorizations;
	}
	
}
