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
	
	void checkSanity();
	
	/**
	 * Save specified depot. Note that oldName and oldOwnerId should not be 
	 * specified together, meaning that you should not rename and transfer 
	 * a depot in a single call
	 * 
	 * @param depot
	 * 			depot to save
	 * @param oldOwnerId
	 * 			in case of transfer, this parameter should hold the id of original 
	 * 			owner when above depot object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if owner is not changed
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above depot object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(Depot depot, @Nullable Long oldOwnerId, @Nullable String oldName);
	
	void delete(Depot depot);
}
