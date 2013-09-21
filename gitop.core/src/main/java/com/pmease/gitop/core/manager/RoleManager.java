package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultRoleManager;
import com.pmease.gitop.core.model.Role;

@ImplementedBy(DefaultRoleManager.class)
public interface RoleManager extends GenericDao<Role> {
	
}
