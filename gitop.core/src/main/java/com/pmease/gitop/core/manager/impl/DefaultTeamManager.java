package com.pmease.gitop.core.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.commons.util.namedentity.NamedEntity;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.permission.operation.GeneralOperation;

@Singleton
public class DefaultTeamManager extends AbstractGenericDao<Team> implements TeamManager {

	@Inject
	public DefaultTeamManager(GeneralDao generalDao) {
		super(generalDao);
	}

	@Sessional
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

	@Override
	public Team getAnonymousTeam(User user) {
		return find(Restrictions.eq("owner", user),
					Restrictions.eq("name", Team.ANONYMOUS));
	}

	@Override
	public Team getLoggedInTeam(User user) {
		return find(Restrictions.eq("owner", user),
				Restrictions.eq("name", Team.LOGGEDIN));
	}

	@Override
	public Team getOwnersTeam(User user) {
		return find(Restrictions.eq("owner", user),
				Restrictions.eq("name", Team.OWNERS));
	}

	@Override
	public GeneralOperation getTeamPermission(Team team) {
		if (team.isAnonymousTeam()) {
			return team.getAuthorizedOperation();
		}
		
		if (team.isOwnersTeam()) {
			return GeneralOperation.ADMIN;
		}
		
		Team anonymous = getAnonymousTeam(team.getOwner());
		
		GeneralOperation permission = anonymous.getAuthorizedOperation();
		if (team.isLoggedInTeam()) {
			return GeneralOperation.higher(team.getAuthorizedOperation(), permission);
		}
		
		Team loggedIn = getLoggedInTeam(team.getOwner());
		permission = GeneralOperation.higher(permission, loggedIn.getAuthorizedOperation());
		return GeneralOperation.higher(team.getAuthorizedOperation(), permission);
	}
}
