package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestManager;
import com.pmease.gitop.core.model.MergeRequest;

@Singleton
public class DefaultMergeRequestManager extends DefaultGenericDao<MergeRequest> implements MergeRequestManager {

	@Inject
	public DefaultMergeRequestManager(GeneralDao generalDao) { 
		super(generalDao);
	}

}
