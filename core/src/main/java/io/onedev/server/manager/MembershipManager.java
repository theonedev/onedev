package io.onedev.server.manager;

import java.util.Collection;

import io.onedev.server.model.Membership;
import io.onedev.server.model.Project;
import io.onedev.server.model.Team;
import io.onedev.server.model.User;
import io.onedev.server.persistence.dao.EntityManager;

public interface MembershipManager extends EntityManager<Membership> {
	
	void delete(Collection<Membership> memberships);
	
	void assignTeams(User user, Project project, Collection<Team> teams);
	
	void assignMembers(Team team, Collection<User> members);
}
