package io.onedev.server.manager;

import java.util.Date;

import javax.annotation.Nullable;

import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.util.facade.ProjectFacade;
import io.onedev.server.util.facade.UserFacade;

public interface UserInfoManager {
	
	void visit(User user, Project project);
	
	@Nullable
	Date getVisitDate(UserFacade user, ProjectFacade project);

}
