package com.pmease.gitop.core.entitymanager.impl;

import java.io.File;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.RepositoryManager;
import com.pmease.gitop.core.model.Repository;

@Singleton
public class DefaultRepositoryManager extends DefaultGenericDao<Repository> implements RepositoryManager {

	public DefaultRepositoryManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Override
	public File locateStorage(Repository repository) {
		//TODO: repository storage
		return null;
	}

}
