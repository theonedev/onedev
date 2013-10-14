package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.MembershipManager;
import com.pmease.gitop.core.model.Membership;

@Singleton
public class DefaultMembershipManager extends AbstractGenericDao<Membership> 
		implements MembershipManager {

	@Inject
	public DefaultMembershipManager(GeneralDao generalDao) {
		super(generalDao);
	}

}
