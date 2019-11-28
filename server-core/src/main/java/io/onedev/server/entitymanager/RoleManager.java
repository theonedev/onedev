package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.Role;
import io.onedev.server.persistence.dao.EntityManager;

public interface RoleManager extends EntityManager<Role> {
	void save(Role role, @Nullable String oldName);
	
	@Nullable
	Role find(String name);
	
	void setupDefaults();
}
