package com.gitplex.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.util.facade.ProjectFacade;
import com.gitplex.server.util.facade.UserFacade;

public interface UserInfoManager {
	
	void visit(User user, Project project);
	
	@Nullable
	Date getVisitDate(UserFacade user, ProjectFacade project);

}
