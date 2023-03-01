package io.onedev.server.web.util.editablebean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.annotation.Editable;

@Editable
public class ProjectAuthorizationsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ProjectAuthorizationBean> authorizations = new ArrayList<>();

	@Editable
	public List<ProjectAuthorizationBean> getAuthorizations() {
		return authorizations;
	}

	public void setAuthorizations(List<ProjectAuthorizationBean> authorizations) {
		this.authorizations = authorizations;
	}
	
}
