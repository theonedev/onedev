package com.pmease.gitplex.core.listeners;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.Repository;

public interface RefListener {
	
	/**
	 * Update or delete a ref. Push operation will wait for completion of this method. If the 
	 * implementation takes a long time, it should be executed in a separate thread in order 
	 * not to block the push operation.
	 *  
	 * @param repository
	 * 			repository to update/delete ref inside
	 * @param refName
	 * 			name of the ref to update/delete
	 * @param newCommitHash 
	 * 			new commit hash of the ref, or <tt>null</tt> if delete a ref
	 */
	void onRefUpdate(Repository repository, String refName, @Nullable String newCommitHash);

}
