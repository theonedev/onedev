package com.pmease.gitop.core.entitymanager.impl;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import com.google.common.base.Preconditions;
import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.gitop.core.entitymanager.AccountManager;
import com.pmease.gitop.core.model.Account;

@Singleton
public class DefaultAccountManager extends DefaultGenericDao<Account> implements AccountManager {

	private volatile Account rootAccount;
	
	public DefaultAccountManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Transactional
	@Override
	public Account getRootAccount() {
		if (rootAccount == null) {
			Criteria criteria = getSession().createCriteria(Account.class).addOrder(Order.asc("id"));
			
			/* the first created account should be root account */
			criteria.setFirstResult(0);
			criteria.setMaxResults(1);
			rootAccount = (Account) criteria.uniqueResult();
			Preconditions.checkNotNull(rootAccount);
		}
		return rootAccount;
	}

}
