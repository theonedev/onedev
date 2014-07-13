package com.pmease.gitop.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.gitop.core.manager.impl.DefaultBranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;

@ImplementedBy(DefaultBranchManager.class)
public interface BranchManager {

	/**
	 * Find branch by repository and branch name.
	 * 
	 * @param repository
	 * 			repository to find branch inside
	 * @param branchName
	 * 			name of the branch to find
	 * @return
	 * 			found branch, or <tt>null</tt> if not found
	 */
	public @Nullable Branch findBy(Repository repository, String branchName);
	
	/**
	 * Find default branch in specified repository.
	 * 
	 * @param repository
	 * 			repository to find default branch
	 * @return
	 * 			found default branch, or <tt>null</tt> if default branch 
	 * 			can not be found
	 */
    public @Nullable Branch findDefault(Repository repository);
    
    /**
     * Delete all refs pointed to this branch and its associated pull requests and 
     * pull request updates.
     * 
     * @param branch
     * 			branch whose refs, pull request refs, and pull request update refs 
     * 			to be deleted
     */
    public void deleteRefs(Branch branch);
    
    /**
     * Delete the branch record from database, as well as removing the branch from 
     * corresponding git repository.
     * 
     * @param branch
     * 			branch to be deleted
     * @param user
     * 			user deleting the branch
     */
    public void delete(Branch branch, @Nullable User user);
    
    /**
     * Create specified branch record in database, and update corresponding git repository to 
     * add the branch.
     * 
     * @param branch
     * 			branch to be created
     * @param commitHash
     * 			commit hash of the branch
     */
    public void create(Branch branch, String commitHash);
    
    /**
     * Rename specified branch record in database, and update corresponding git repository to 
     * reflect the renaming.
     *   
     * @param branch
     * 			branch to be renamed
     * @param newName
     * 			new name of the branch
     */
    public void rename(Branch branch, String newName);
    
    public void save(Branch branch);
    
    public void trim(Collection<Long> branchIds);
    
}
