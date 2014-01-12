package com.pmease.gitop.core.manager.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.TeamManager;
import com.pmease.gitop.core.manager.UserManager;
import com.pmease.gitop.model.Team;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.permission.operation.GeneralOperation;

@Singleton
public class DefaultTeamManager extends AbstractGenericDao<Team> implements TeamManager {

	private static class BuiltInTeam {
		final Long anonymousId;
		final Long ownersId;
		final Long loggedInId;
		
		BuiltInTeam(Long anonymousId, Long ownersId, Long loggedInId) {
			this.anonymousId = anonymousId;
			this.ownersId = ownersId;
			this.loggedInId = loggedInId;
		}
	}
	
	private final LoadingCache<Long, BuiltInTeam> builtInTeamsCache;
	
	@Inject
	public DefaultTeamManager(GeneralDao generalDao, final UserManager userManager) {
		super(generalDao);
		
		builtInTeamsCache =
				CacheBuilder.newBuilder()
					.build(new CacheLoader<Long, BuiltInTeam>() {

						@Override
						public BuiltInTeam load(Long key) throws Exception {
							User user = userManager.get(key);
							Team anonymous = findBy(user, Team.ANONYMOUS);
							Team owners = findBy(user, Team.OWNERS);
							Team loggedIn = findBy(user, Team.LOGGEDIN);
							return new BuiltInTeam(anonymous.getId(), owners.getId(), loggedIn.getId());
						}
					});
	}

	@Sessional
	@Override
	public Team findBy(User owner, String teamName) {
		return find(new Criterion[]{Restrictions.eq("owner", owner), Restrictions.eq("name", teamName)});
	}

	@Transactional
	@Override
	public Team getAnonymous(User user) {
		return load(builtInTeamsCache.getUnchecked(user.getId()).anonymousId);
	}

	@Override
	public Team getLoggedIn(User user) {
		return load(builtInTeamsCache.getUnchecked(user.getId()).loggedInId);
	}

	@Override
	public Team getOwners(User user) {
		return load(builtInTeamsCache.getUnchecked(user.getId()).ownersId);
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

	@Override
	public void trim(Collection<Long> teamIds) {
		for (Iterator<Long> it = teamIds.iterator(); it.hasNext();) {
			if (get(it.next()) == null)
				it.remove();
		}
	}
}
