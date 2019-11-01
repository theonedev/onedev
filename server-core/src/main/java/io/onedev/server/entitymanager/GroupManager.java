package io.onedev.server.entitymanager;

import javax.annotation.Nullable;

import io.onedev.server.model.Group;
import io.onedev.server.persistence.dao.EntityManager;

public interface GroupManager extends EntityManager<Group> {
	/**
	 * Save specified group
	 * 
	 * @param group
	 * 			group to save
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above group object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(Group group, @Nullable String oldName);
	
	@Nullable
	Group find(String name);
	
	@Nullable
	Group findAnonymous();
}
