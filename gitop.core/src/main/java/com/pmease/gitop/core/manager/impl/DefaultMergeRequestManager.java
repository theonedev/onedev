package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;

import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestManager;
import com.pmease.gitop.core.model.MergeRequest;

@Singleton
public class DefaultMergeRequestManager extends DefaultGenericDao<MergeRequest> implements MergeRequestManager {

	@Inject
	public DefaultMergeRequestManager(GeneralDao generalDao, Provider<Session> sessionProvider) { 
		super(generalDao, sessionProvider);
	}

}
