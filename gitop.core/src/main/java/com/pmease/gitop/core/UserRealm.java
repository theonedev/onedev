package com.pmease.gitop.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.Permission;

import com.pmease.commons.persistence.dao.GeneralDao;
import com.pmease.commons.security.AbstractRealm;
import com.pmease.gitop.core.entitymanager.GroupManager;
import com.pmease.gitop.core.model.User;

@Singleton
public class UserRealm extends AbstractRealm<User> {

	private final GroupManager groupManager;

	@Inject
	public UserRealm(GeneralDao generalDao, CredentialsMatcher credentialsMatcher, GroupManager groupManager) {
		super(generalDao, credentialsMatcher);
		
		this.groupManager = groupManager;
	}

	@Override
	protected Collection<Permission> permissionsOf(Long accountId) {
		Collection<Permission> permissions = new ArrayList<Permission>();
		
		permissions.addAll(groupManager.getGroups(accountId));

		return permissions;
	}

}
