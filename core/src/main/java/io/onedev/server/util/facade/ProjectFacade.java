package io.onedev.server.util.facade;

import java.util.Date;

import io.onedev.server.OneDev;
import io.onedev.server.manager.UserInfoManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ProjectPrivilege;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final ProjectPrivilege defaultPrivilege;
	
	private final String uuid;
	
	public ProjectFacade(Project project) {
		super(project.getId());
		name = project.getName();
		defaultPrivilege = project.getDefaultPrivilege();
		uuid = project.getUUID();
	}
	
	public String getName() {
		return name;
	}

	public ProjectPrivilege getDefaultPrivilege() {
		return defaultPrivilege;
	}

	public String getUUID() {
		return uuid;
	}

	public static int compareLastVisit(ProjectFacade project1, ProjectFacade project2) {
		User user = SecurityUtils.getUser();
		if (user != null) {
			UserInfoManager userInfoManager = OneDev.getInstance(UserInfoManager.class);
			UserFacade userFacade = user.getFacade();
			Date date1 = userInfoManager.getVisitDate(userFacade, project1);
			Date date2 = userInfoManager.getVisitDate(userFacade, project2);
			if (date1 != null) {
				if (date2 != null)
					return date2.compareTo(date1);
				else
					return -1;
			} else {
				if (date2 != null)
					return 1;
				else
					return project1.compareTo(project2);
			}
		} else {
			return project1.compareTo(project2);
		}
	}
	
}
