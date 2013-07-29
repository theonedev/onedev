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
import com.pmease.gitop.core.model.Account;

@Singleton
public class AccountRealm extends AbstractRealm<Account> {

	private final TeamManager teamManager;

	private final RoleManager roleManager;
	
	@Inject
	public AccountRealm(GeneralDao generalDao, CredentialsMatcher credentialsMatcher, 
			TeamManager teamManager, RoleManager roleManager) {
		super(generalDao, credentialsMatcher);
		
		this.teamManager = teamManager;
		this.roleManager = roleManager;
	}

	@Override
	protected Collection<Permission> permissionsOf(Long accountId) {
		Collection<Permission> permissions = new ArrayList<Permission>();
		
		permissions.addAll(roleManager.getRoles(accountId));
		permissions.addAll(teamManager.getTeams(accountId));

		return permissions;
	}

}
