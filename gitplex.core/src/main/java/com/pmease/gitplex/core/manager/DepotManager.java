package com.pmease.gitplex.core.manager;

import javax.annotation.Nullable;

import com.pmease.commons.hibernate.dao.EntityDao;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;

public interface DepotManager extends EntityDao<Depot> {
	
	@Nullable Depot findBy(String ownerName, String depotName);
	
	@Nullable Depot findBy(Account owner, String depotName);

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
	Depot fork(Depot depot, Account user);
	
	void checkSanity(Depot depot);
	
	void checkSanity();
	
	/**
	 * Save depot. Note that depot name should not be changed via this method. 
	 * Call {@link #rename(Account, Long, String, String)} if you want to rename
	 * the depot. 
	 * 
	 * @param depot
	 */
	void save(Depot depot);
	
	void delete(Depot depot);
	
	void rename(Account depotOwner, Long depotId, String oldName, String newName);
}
