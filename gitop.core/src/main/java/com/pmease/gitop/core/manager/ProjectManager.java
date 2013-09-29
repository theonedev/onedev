package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.storage.ProjectStorage;

@ImplementedBy(DefaultProjectManager.class)
public interface ProjectManager extends GenericDao<Project> {
	
	ProjectStorage locateStorage(Project project);
	
	Project find(String ownerName, String projectName);
	
	Collection<Project> findPublic();
}
