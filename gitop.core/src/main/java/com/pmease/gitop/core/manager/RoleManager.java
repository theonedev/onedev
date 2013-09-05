package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultRoleManager;
import com.pmease.gitop.core.model.Role;

@ImplementedBy(DefaultRoleManager.class)
public interface RoleManager extends GenericDao<Role> {
	
	Collection<Role> findAnonymousRoles();
	
	Collection<Role> findRegisterRoles();
}
