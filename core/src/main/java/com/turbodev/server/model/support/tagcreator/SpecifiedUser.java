package com.turbodev.server.model.support.tagcreator;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;
import com.turbodev.server.util.editable.annotation.UserChoice;

@Editable(order=400, name="Specified User")
public class SpecifiedUser implements TagCreator {

	private static final long serialVersionUID = 1L;

	private String userName;

	@Editable(name="User")
	@UserChoice(type=UserChoice.Type.PROJECT_WRITER)
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String getNotMatchMessage(Project project, User user) {
		UserManager userManager = TurboDev.getInstance(UserManager.class);
		User specifiedUser = Preconditions.checkNotNull(userManager.findByName(userName));
		if (!specifiedUser.equals(user)) 
			return "This operation can only be performed by user: " + userName;
		else 
			return null;
	}

}
