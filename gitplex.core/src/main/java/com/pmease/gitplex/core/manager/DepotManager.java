package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.User;

public interface DepotManager extends Dao {
	
	@Nullable Depot findBy(String ownerName, String depotName);
	
	@Nullable Depot findBy(User owner, String depotName);

	@Nullable Depot findBy(String depotFQN);

	/**
	 * Fork specified repository as specified user.
	 * 
	 * @param depot
	 * 			repository to be forked
	 * @param user
	 * 			user forking the repository
	 * @return
	 * 			newly forked repository. If the repository has already been forked, return the 
	 * 			repository forked previously
	 */
	Depot fork(Depot depot, User user);
	
	void checkSanity(Depot depot);
	
	void checkSanity();
	
	/**
	 * Save depot. Note that depot name should not be changed via this method. 
	 * Call {@link #rename(User, Long, String, String)} if you want to rename
	 * the depot. 
	 * 
	 * @param depot
	 */
	void save(Depot depot);
	
	void delete(Depot depot);
	
	void rename(User depotOwner, Long depotId, String oldName, String newName);
}
