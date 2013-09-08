package com.pmease.gitop.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.model.Repository;

@Singleton
public class DefaultRepositoryManager extends DefaultGenericDao<Repository> implements RepositoryManager {

	@Inject
	public DefaultRepositoryManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Override
	public File locateStorage(Repository repository) {
		//TODO: repository storage
		return null;
	}

}
