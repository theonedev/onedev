package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.Repository;

import com.pmease.commons.hibernate.dao.EntityManager;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;

public interface DepotManager extends EntityManager<Depot> {
	
	@Nullable Depot findBy(String accountName, String depotName);
	
	@Nullable Depot findBy(Account account, String depotName);

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
	
	/**
	 * Save specified depot. Note that oldName and oldAccountId should not be 
	 * specified together, meaning that you should not rename and transfer 
	 * a depot in a single call
	 * 
	 * @param depot
	 * 			depot to save
	 * @param oldAccountId
	 * 			in case of transfer, this parameter should hold the id of original 
	 * 			account when above depot object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if account is not changed
	 * @param oldName
	 * 			in case of rename, this parameter should hold the original name 
	 * 			when above depot object is initially loaded to ensure database 
	 * 			integrity. Use <tt>null</tt> if original name does not exist, 
	 * 			or the name is not changed
	 */
	void save(Depot depot, @Nullable Long oldAccountId, @Nullable String oldName);
	
	void delete(Depot depot);
	
	Repository getRepository(Depot depot);
	
	Collection<Depot> getAccessibles(@Nullable Account account, @Nullable Account user);

}
