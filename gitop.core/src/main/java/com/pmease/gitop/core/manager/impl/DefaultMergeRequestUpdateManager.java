package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestUpdateManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;

@Singleton
public class DefaultMergeRequestUpdateManager extends DefaultGenericDao<MergeRequestUpdate> 
		implements MergeRequestUpdateManager {

	@Inject
	public DefaultMergeRequestUpdateManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

}
