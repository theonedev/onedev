package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.UserAuthorizationByIndividualManager;
import com.pmease.gitop.core.model.UserAuthorizationByIndividual;

@Singleton
public class DefaultUserAuthorizationByIndividualManager 
		extends DefaultGenericDao<UserAuthorizationByIndividual> 
		implements UserAuthorizationByIndividualManager {

	@Inject
	public DefaultUserAuthorizationByIndividualManager(GeneralDao generalDao) {
		super(generalDao);
	}

}
