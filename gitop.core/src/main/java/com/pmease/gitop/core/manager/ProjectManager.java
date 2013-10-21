package com.pmease.gitop.core.manager;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultProjectManager;
import com.pmease.gitop.core.model.Project;
import com.pmease.gitop.core.model.User;

@ImplementedBy(DefaultProjectManager.class)
public interface ProjectManager extends GenericDao<Project> {
	
	@Nullable Project find(String ownerName, String projectName);
	
	@Nullable Project find(User owner, String projectName);
	
	Set<String> getReservedNames();

	Collection<Project> findPublic();
}
