package com.pmease.gitop.core.entitymanager.impl;

import javax.inject.Provider;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.UserManager;
import com.pmease.gitop.core.model.User;

public class DefaultUserManager extends DefaultGenericDao<User> implements UserManager {


	public DefaultUserManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Transactional
	@Override
	public User findRoot() {
		Criteria criteria = getSession().createCriteria(User.class).addOrder(Order.asc("id"));
		
		/* the first created user should be root user */
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (User) criteria.uniqueResult();
	}

}
