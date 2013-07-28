package com.pmease.gitop.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.security.AbstractRealm;
import com.pmease.gitop.core.entitymanager.TeamManager;
import com.pmease.gitop.core.entitymanager.UserManager;
import com.pmease.gitop.core.model.Team;
import com.pmease.gitop.core.model.User;

@Singleton
public class UserRealm extends AbstractRealm<User> {

	private final TeamManager teamManager;
	
	private final UserManager userManager;
	
	@Inject
	public UserRealm(GeneralDao generalDao, CredentialsMatcher credentialsMatcher, 
			TeamManager teamManager, UserManager userManager) {
		super(generalDao, credentialsMatcher);
		this.teamManager = teamManager;
		this.userManager = userManager;
	}

	@Override
	protected Collection<Permission> permissionsOf(Long userId) {
		Collection<Permission> permissions = new ArrayList<Permission>();
		
		permissions.add(userManager.load(userId).getAuthorization().getRole());
		for (Team team: teamManager.getTeams(userId))
			permissions.add(team);
		return permissions;
	}

}
