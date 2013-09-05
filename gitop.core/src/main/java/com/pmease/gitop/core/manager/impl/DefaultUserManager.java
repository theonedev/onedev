package com.pmease.gitop.core.manager.impl;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;

@Singleton
public class DefaultUserManager extends DefaultGenericDao<User> implements UserManager {

	private volatile User rootUser;
	
	public DefaultUserManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Transactional
	@Override
	public User getRootUser() {
		if (rootUser == null) {
			Criteria criteria = getSession().createCriteria(User.class).addOrder(Order.asc("id"));
			
			/* the first created user should be root user */
			criteria.setFirstResult(0);
			criteria.setMaxResults(1);
			rootUser = (User) criteria.uniqueResult();
			Preconditions.checkNotNull(rootUser);
		}
		return rootUser;
	}
	
	@Transactional
	@Override
	public User find(String userName) {
		return find(new Criterion[]{Restrictions.eq("name", userName)});
	}

	@Override
	public EntityLoader asEntityLoader() {
		return new EntityLoader() {

			@Override
			public NamedEntity get(final Long id) {
				final User user = DefaultUserManager.this.get(id);
				if (user != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return id;
						}

						@Override
						public String getName() {
							return user.getName();
						}
						
					};
				} else {
					return null;
				}
			}

			@Override
			public NamedEntity get(String name) {
				final User user = find(name);
				if (user != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return user.getId();
						}

						@Override
						public String getName() {
							return user.getName();
						}
						
					};
				} else {
					return null;
				}
			}
			
		};
	}

}
