package com.pmease.gitop.core.manager;

import java.io.File;
import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultRepositoryManager;
import com.pmease.gitop.core.model.Repository;

@ImplementedBy(DefaultRepositoryManager.class)
public interface RepositoryManager extends GenericDao<Repository> {
	
	File locateStorage(Repository repository);
	
	Repository find(String ownerName, String repositoryName);
	
	Collection<Repository> findPublic();
}
