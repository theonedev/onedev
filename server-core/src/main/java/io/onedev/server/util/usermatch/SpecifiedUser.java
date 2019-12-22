package io.onedev.server.util.usermatch;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;

public class SpecifiedUser implements UserMatchCriteria {

	private static final long serialVersionUID = 1L;
	
	private String userName;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public boolean matches(Project project, User user) {
		UserManager userManager = OneDev.getInstance(UserManager.class);
		User specifiedUser = Preconditions.checkNotNull(userManager.findByName(userName));
		return specifiedUser.equals(user);
	}

	@Override
	public String toString() {
		return "user(" + StringUtils.escape(userName, "()") + ")";
	}
	
}
