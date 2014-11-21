package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;

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

	public @Nullable Branch findBy(String branchPath);
	
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
     */
    public void delete(Branch branch);
    
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
    
    public void save(Branch branch);
    
    public void trim(Collection<Long> branchIds);
    
}
