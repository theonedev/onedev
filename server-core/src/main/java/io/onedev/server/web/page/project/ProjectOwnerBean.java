package io.onedev.server.web.page.project;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.UserChoice;

@Editable
public class ProjectOwnerBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String owner;

	@Editable
	@UserChoice
	@NotEmpty
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
}
