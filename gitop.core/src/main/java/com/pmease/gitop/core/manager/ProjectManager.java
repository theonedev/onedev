package com.pmease.gitop.core.manager;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultProjectManager;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.User;

@ImplementedBy(DefaultProjectManager.class)
public interface ProjectManager extends GenericDao<Project> {
	
	@Nullable Project findBy(String ownerName, String projectName);
	
	@Nullable Project findBy(User owner, String projectName);

	/**
	 * Fork specified project as specified user.
	 * 
	 * @param project
	 * 			project to be forked
	 * @param user
	 * 			user forking the project
	 * @return
	 * 			newly forked project. If the project has already been forked, return the 
	 * 			project forked previously
	 */
	Project fork(Project project, User user);
	
	void checkSanity();
	
	void checkSanity(Project project);
}
