package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.UserAuthorizationByIndividualManager;
import com.pmease.gitop.core.model.UserAuthorizationByIndividual;

@Singleton
public class DefaultUserAuthorizationByIndividualManager 
		extends AbstractGenericDao<UserAuthorizationByIndividual> 
		implements UserAuthorizationByIndividualManager {

	@Inject
	public DefaultUserAuthorizationByIndividualManager(GeneralDao generalDao) {
		super(generalDao);
	}

}
