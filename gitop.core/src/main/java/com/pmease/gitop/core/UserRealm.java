package com.pmease.gitop.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.security.AbstractRealm;
import com.pmease.gitop.core.entitymanager.RoleManager;
import com.pmease.gitop.core.entitymanager.TeamManager;
import com.pmease.gitop.core.entitymanager.UserManager;
import com.pmease.gitop.core.model.RoleMembership;
import com.pmease.gitop.core.model.TeamMembership;
import com.pmease.gitop.core.model.User;
import com.pmease.gitop.core.model.permission.ObjectPermission;

@Singleton
public class UserRealm extends AbstractRealm<User> {

	private final UserManager userManager;
	
	private final RoleManager roleManager;
	
	private final TeamManager teamManager;
	
	@Inject
	public UserRealm(GeneralDao generalDao, CredentialsMatcher credentialsMatcher, 
			UserManager userManager, RoleManager roleManager, TeamManager teamManager) {
		super(generalDao, credentialsMatcher);

		this.userManager = userManager;
		this.roleManager = roleManager;
		this.teamManager = teamManager;
	}

	@Override
	protected Collection<Permission> permissionsOf(Long userId) {
		Collection<Permission> permissions = new ArrayList<Permission>();
		
		if (userId != 0L) {
			User user = userManager.load(userId);

			for (RoleMembership membership: user.getRoleMemberships())
				permissions.add(membership.getRole());
			
			for (TeamMembership membership: user.getTeamMemberships())
				permissions.add(membership.getTeam());
			
			/* an user is administrator of its own account */
			permissions.add(ObjectPermission.ofUserAdmin(user));
		} else {
			permissions.addAll(roleManager.getAnonymousRoles());
			permissions.addAll(teamManager.getAnonymousTeams());
		}

		return permissions;
	}

}
