package io.onedev.server.util.inputspec.userchoiceinput.choiceprovider;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.model.Group;
import io.onedev.server.model.User;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.OmitName;
import io.onedev.server.web.editable.annotation.GroupChoice;

@Editable(order=150, name="Users belonging to group")
public class GroupUsers implements ChoiceProvider {

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
	public List<UserFacade> getChoices(boolean allPossible) {
		List<UserFacade> choices = new ArrayList<>();
		Group group = OneDev.getInstance(GroupManager.class).find(groupName);
		if (group != null) {
			for (User user: group.getMembers())
				choices.add(user.getFacade());
		}
		return choices;
	}

}
