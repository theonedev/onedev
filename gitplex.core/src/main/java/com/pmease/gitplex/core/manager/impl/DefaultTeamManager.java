package com.pmease.gitplex.core.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.DefaultDao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.User;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.TeamManager;
import com.pmease.gitplex.core.manager.UserManager;

@Singleton
public class DefaultTeamManager extends DefaultDao implements TeamManager {

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

	private final UserManager userManager;
	
	private final LoadingCache<Long, BuiltInTeam> builtInTeamsCache;
	
	@Inject
	public DefaultTeamManager(Provider<Session> sessionProvider, final UserManager userManager) {
		super(sessionProvider);
		
		this.userManager = userManager;
		builtInTeamsCache =
				CacheBuilder.newBuilder()
					.build(new CacheLoader<Long, BuiltInTeam>() {

						@Override
						public BuiltInTeam load(Long key) throws Exception {
							User user = get(User.class, key);
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
		return find(EntityCriteria.of(Team.class)
				.add(Restrictions.eq("owner", owner))
				.add(Restrictions.eq("name", teamName)));
	}
	
	@Sessional
	@Override
	public Team findBy(String teamFQN) {
    	String userName = StringUtils.substringBefore(teamFQN, Depot.FQN_SEPARATOR);
    	User user = userManager.findByName(userName);
    	if (user != null)
    		return findBy(user, StringUtils.substringAfter(teamFQN, Depot.FQN_SEPARATOR));
    	else
    		return null;
	}

	@Override
	public Team getAnonymous(User user) {
		return load(Team.class, builtInTeamsCache.getUnchecked(user.getId()).anonymousId);
	}

	@Override
	public Team getLoggedIn(User user) {
		return load(Team.class, builtInTeamsCache.getUnchecked(user.getId()).loggedInId);
	}

	@Override
	public Team getOwners(User user) {
		return load(Team.class, builtInTeamsCache.getUnchecked(user.getId()).ownersId);
	}

	@Transactional
	@Override
	public void delete(Team team) {
		for (Depot each: team.getOwner().getDepots()) {
			for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onTeamDelete(team))
					it.remove();
			}
		}
	}

	@Transactional
	@Override
	public void rename(User teamOwner, String oldName, String newName) {
		Query query = getSession().createQuery("update Team set name=:newName where "
				+ "owner=:owner and name=:oldName");
		query.setParameter("owner", teamOwner);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
		
		for (Depot depot: teamOwner.getDepots()) {
			for (GateKeeper gateKeeper: depot.getGateKeepers()) {
				gateKeeper.onTeamRename(oldName, newName);
			}
		}
	}

}
