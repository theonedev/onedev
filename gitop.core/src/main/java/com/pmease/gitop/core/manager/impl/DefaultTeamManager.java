package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@Singleton
public class DefaultTeamManager extends AbstractGenericDao<Team> implements TeamManager {

	private volatile Long anonymousId;
	
	private volatile Long ownersId;
	
	private volatile Long loggedInId;
	
	@Inject
	public DefaultTeamManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
	@Override
	public Team findBy(User owner, String teamName) {
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
				final Team team = findBy(owner, name);
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

	@Transactional
	@Override
	public Team getAnonymous(User user) {
		Team anonymous;
		if (anonymousId == null) {
			anonymous = findBy(user, Team.ANONYMOUS);
			Preconditions.checkNotNull(anonymous);
			anonymousId = anonymous.getId();
		} else {
			anonymous = load(anonymousId);
		}
		return anonymous;
	}

	@Override
	public Team getLoggedIn(User user) {
		Team loggedIn;
		if (loggedInId == null) {
			loggedIn = findBy(user, Team.LOGGEDIN);
			Preconditions.checkNotNull(loggedIn);
			loggedInId = loggedIn.getId();
		} else {
			loggedIn = load(loggedInId);
		}
		return loggedIn;
	}

	@Override
	public Team getOwners(User user) {
		Team owners;
		if (ownersId == null) {
			owners = findBy(user, Team.OWNERS);
			Preconditions.checkNotNull(owners);
			ownersId = owners.getId();
		} else {
			owners = load(ownersId);
		}
		return owners;
	}

	@Override
	public GeneralOperation getActualAuthorizedOperation(Team team) {
		if (team.isOwners()) {
			return GeneralOperation.ADMIN;
		} else if (team.isAnonymous()) {
			return team.getAuthorizedOperation();
		} else if (team.isLoggedIn()) {
			return GeneralOperation.mostPermissive(team.getAuthorizedOperation(), 
					getAnonymous(team.getOwner()).getAuthorizedOperation());
		} else {
			return GeneralOperation.mostPermissive(team.getAuthorizedOperation(), 
					getAnonymous(team.getOwner()).getAuthorizedOperation(), 
					getLoggedIn(team.getOwner()).getAuthorizedOperation());
		}
	}
}
