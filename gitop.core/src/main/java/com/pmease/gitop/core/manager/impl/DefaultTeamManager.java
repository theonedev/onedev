package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

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
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;

@Singleton
public class DefaultTeamManager extends DefaultGenericDao<Team> implements TeamManager {

	@Inject
	public DefaultTeamManager(GeneralDao generalDao, Provider<Session> sessionProvider) {
		super(generalDao, sessionProvider);
	}

	@Transactional
	@Override
	public Collection<Team> findAnonymousTeams() {
		return query(new Criterion[]{Restrictions.eq("anonymous", true)});
	}

	@Transactional
	@Override
	public Collection<Team> findRegisterTeams() {
		return query(new Criterion[]{Restrictions.eq("register", true)});
	}
	
	@Transactional
	@Override
	public Team find(User owner, String teamName) {
		return find(new Criterion[]{Restrictions.eq("owner", owner), Restrictions.eq("name", teamName)});
	}

	@Override
	public EntityLoader asEntityLoader(final User owner) {
		return new EntityLoader() {

			@Override
			public NamedEntity get(final Long id) {
				final Team team = DefaultTeamManager.this.get(id);
				if (team != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return id;
						}

						@Override
						public String getName() {
							return team.getName();
						}
						
					};
				} else {
					return null;
				}
			}

			@Override
			public NamedEntity get(String name) {
				final Team team = find(owner, name);
				if (team != null) {
					return new NamedEntity() {

						@Override
						public Long getId() {
							return team.getId();
						}

						@Override
						public String getName() {
							return team.getName();
						}
						
					};
				} else {
					return null;
				}
			}
			
		};
	}

}
