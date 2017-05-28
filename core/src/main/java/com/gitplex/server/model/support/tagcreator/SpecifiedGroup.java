package com.gitplex.server.model.support.tagcreator;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.GroupManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.Group;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.GroupChoice;
import com.google.common.base.Preconditions;

@Editable(order=300, name="Specified Group")
public class SpecifiedGroup implements TagCreator {

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
	public String getNotMatchMessage(Project project, User user) {
		GroupManager groupManager = GitPlex.getInstance(GroupManager.class);
		Group group = Preconditions.checkNotNull(groupManager.find(groupName));
		if (!group.getMembers().contains(user)) 
			return "This operation can only be performed by group: " + groupName;
		else 
			return null;
	}

}
