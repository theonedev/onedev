package com.gitplex.server.model.support.tagcreator;

import org.hibernate.validator.constraints.NotEmpty;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.User;
import com.gitplex.server.model.Project;
import com.gitplex.server.util.editable.annotation.UserChoice;
import com.gitplex.server.util.editable.annotation.Editable;
import com.google.common.base.Preconditions;

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
		UserManager userManager = GitPlex.getInstance(UserManager.class);
		User specifiedUser = Preconditions.checkNotNull(userManager.findByName(userName));
		if (!specifiedUser.equals(user)) 
			return "This operation can only be performed by user: " + userName;
		else 
			return null;
	}

}
