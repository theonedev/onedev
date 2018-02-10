package com.turbodev.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import com.turbodev.server.model.Project;
import com.turbodev.server.model.User;
import com.turbodev.server.util.facade.ProjectFacade;
import com.turbodev.server.util.facade.UserFacade;

public interface UserInfoManager {
	
	void visit(User user, Project project);
	
	@Nullable
	Date getVisitDate(UserFacade user, ProjectFacade project);

}
