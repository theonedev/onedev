package com.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.Repository;

import com.gitplex.core.entity.Account;
import com.gitplex.core.entity.Depot;
import com.gitplex.commons.hibernate.dao.EntityManager;

public interface DepotManager extends EntityManager<Depot> {
	
	@Nullable Depot find(String accountName, String depotName);
	
	@Nullable Depot find(Account account, String depotName);

	@Nullable Depot find(String depotFQN);

	void fork(Depot from, Depot to);
	
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
	
	Repository getRepository(Depot depot);
	
	Collection<Depot> findAllAccessible(@Nullable Account account, @Nullable Account user);

}
