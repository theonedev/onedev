package com.pmease.gitop.core.manager.impl;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.model.Repository;

@Singleton
public class DefaultRepositoryManager extends DefaultGenericDao<Repository> implements RepositoryManager {

	@Inject
	public DefaultRepositoryManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Override
	public File locateStorage(Repository repository) {
		//TODO: repository storage
		return null;
	}

}
