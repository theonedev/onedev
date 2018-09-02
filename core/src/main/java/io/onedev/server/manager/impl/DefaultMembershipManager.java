package io.onedev.server.manager.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.onedev.server.manager.MembershipManager;
import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultMembershipManager extends AbstractEntityManager<Membership> implements MembershipManager {

	@Inject
	public DefaultMembershipManager(Dao dao) {
		super(dao);
	}

	@Transactional
	@Override
	public void delete(Collection<Membership> memberships) {
		for (Membership membership: memberships)
			dao.remove(membership);
	}

	@Transactional
	@Override
	public void assignTeams(User user, Project project, Collection<Team> teams) {
		for (Team team: teams) {
			if (team.getProject().equals(project)) {
				boolean found = false;
				for (Membership membership: user.getMemberships()) {
					if (membership.getTeam().equals(team)) {
						found = true;
						break;
					}
				}
				if (!found) {
					Membership membership = new Membership();
					membership.setTeam(team);
					membership.setUser(user);
					save(membership);
					user.getMemberships().add(membership);
				}
			}
		}
		for (Iterator<Membership> it = user.getMemberships().iterator(); it.hasNext();) {
			Membership membership = it.next();
			if (membership.getTeam().getProject().equals(project)) {
				boolean found = false;
				for (Team team: teams) {
					if (team.equals(membership.getTeam())) {
						found = true;
						break;
					}
				}
				if (!found) {
					delete(membership);
					it.remove();
				}
			}
		}
	}

	@Transactional
	@Override
	public void assignMembers(Team team, Collection<User> members) {
		for (User member: members) {
			boolean found = false;
			for (Membership membership: team.getMemberships()) {
				if (membership.getUser().equals(member)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Membership membership = new Membership();
				membership.setTeam(team);
				membership.setUser(member);
				save(membership);
				team.getMemberships().add(membership);
			}
		}
		for (Iterator<Membership> it = team.getMemberships().iterator(); it.hasNext();) {
			Membership membership = it.next();
			boolean found = false;
			for (User member: members) {
				if (member.equals(membership.getUser())) {
					found = true;
					break;
				}
			}
			if (!found) {
				delete(membership);
				it.remove();
			}
		}
	}

}
