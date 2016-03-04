package com.pmease.gitplex.core.extensionpoint;

import org.eclipse.jgit.lib.ObjectId;

import com.pmease.gitplex.core.entity.Depot;

public interface RefListener {
	
	/**
	 * Update or delete a ref. Push operation will wait for completion of this method. If the 
	 * implementation takes a long time, it should be executed in a separate thread in order 
	 * not to block the push operation.
	 *  
	 * @param depot
	 * 			repository to update/delete ref inside
	 * @param refName
	 * 			name of the ref to update/delete
	 * @param oldCommit
	 * 			old commit of the ref
	 * @param newCommit
	 * 			new commit of the ref
	 */
	void onRefUpdate(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit);

}
