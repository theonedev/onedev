package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.persistence.Transactional;
import com.pmease.commons.persistence.dao.DefaultGenericDao;
import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.Repository;

@Singleton
public class DefaultBranchManager extends DefaultGenericDao<Branch> implements BranchManager {

	@Inject
	public DefaultBranchManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	
	@Transactional
	@Override
	public Branch find(Repository repository, String name) {
		return find(new Criterion[]{Restrictions.eq("repository", repository), Restrictions.eq("name", name)});
	}

	@Override
	public EntityLoader asEntityLoader(final Repository repository) {
		return new EntityLoader() {

			@Override
			public NamedEntity get(final Long id) {
				final Branch branch = DefaultBranchManager.this.get(id);
				if (branch != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return id;
						}

						@Override
						public String getName() {
							return branch.getName();
						}
						
					};
				} else {
					return null;
				}
			}

			@Override
			public NamedEntity get(String name) {
				final Branch branch = find(repository, name);
				if (branch != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return branch.getId();
						}

						@Override
						public String getName() {
							return branch.getName();
						}
						
					};
				} else {
					return null;
				}
			}
			
		};
	}
}
