package io.onedev.server.web.page.project.setting.team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.UserChoice;

@Editable
public class MembersEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> members = new ArrayList<>();

	@Editable
	@UserChoice
	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}
	
}
