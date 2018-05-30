package io.onedev.server.model.support.ifsubmittedby;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;

import io.onedev.server.OneDev;
import io.onedev.server.manager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.GroupChoice;
import io.onedev.server.web.editable.annotation.OmitName;

@Editable(order=300, name="Group")
public class SpecifiedGroup implements IfSubmittedBy {

	private static final long serialVersionUID = 1L;

	private String groupName;

	@Editable(name="Group")
	@GroupChoice
	@OmitName
	@NotEmpty
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public boolean matches(Project project, User user) {
		GroupManager groupManager = OneDev.getInstance(GroupManager.class);
		Group group = Preconditions.checkNotNull(groupManager.find(groupName));
		return group.getMembers().contains(user);
	}

}
