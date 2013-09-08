package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.DefaultGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.core.model.User;

@Singleton
public class DefaultUserManager extends DefaultGenericDao<User> implements UserManager {

	private volatile Long rootUserId;
	
	@Inject
	public DefaultUserManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Transactional
	@Override
	public User getRootUser() {
		User rootUser;
		if (rootUserId == null) {
			// The first created user should be root user 
			rootUser = find(null, new Order[]{Order.asc("id")});
			Preconditions.checkNotNull(rootUser);
			rootUserId = rootUser.getId();
		} else {
			rootUser = load(rootUserId);
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
