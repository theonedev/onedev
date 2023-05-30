package io.onedev.server.web.page.project.setting.authorization;

import io.onedev.server.annotation.Editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable
public class GroupAuthorizationsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<GroupAuthorizationBean> authorizations = new ArrayList<>();

	@Editable
	public List<GroupAuthorizationBean> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<GroupAuthorizationBean> authorizations) {
		this.authorizations = authorizations;
	}
	
}
