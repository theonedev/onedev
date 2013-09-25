package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.MergeRequestUpdateManager;
import com.pmease.gitop.core.model.MergeRequestUpdate;

@Singleton
public class DefaultMergeRequestUpdateManager extends AbstractGenericDao<MergeRequestUpdate> 
		implements MergeRequestUpdateManager {

	@Inject
	public DefaultMergeRequestUpdateManager(GeneralDao generalDao) {
		super(generalDao);
	}

}
