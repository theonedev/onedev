package com.turbodev.server.model.support.submitter;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Preconditions;
import com.turbodev.server.TurboDev;
import com.turbodev.server.manager.UserManager;
import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.editable.annotation.Editable;
import com.turbodev.server.util.editable.annotation.UserChoice;

@Editable(order=200, name="Specified user")
public class SpecifiedUser implements Submitter {

	private static final long serialVersionUID = 1L;
	
	private String userName;

	@Editable(name="User")
	@UserChoice
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public boolean matches(Project project, User user) {
		UserManager userManager = TurboDev.getInstance(UserManager.class);
		User specifiedUser = Preconditions.checkNotNull(userManager.findByName(userName));
		return specifiedUser.equals(user);
	}

}
