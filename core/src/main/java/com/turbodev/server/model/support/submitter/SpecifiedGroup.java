package com.turbodev.server.model.support.submitter;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.GroupManager;
import com.turbodev.server.model.Group;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;
import com.turbodev.server.util.editable.annotation.GroupChoice;

@Editable(order=300, name="Specified group")
public class SpecifiedGroup implements Submitter {

	private static final long serialVersionUID = 1L;

	private String groupName;

	@Editable(name="Group")
	@GroupChoice
	@NotEmpty
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public boolean matches(Project project, User user) {
		GroupManager groupManager = TurboDev.getInstance(GroupManager.class);
		Group group = Preconditions.checkNotNull(groupManager.find(groupName));
		return group.getMembers().contains(user);
	}

}
