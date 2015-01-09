package com.pmease.gitplex.core.manager;

import java.util.Collection;

import javax.annotation.Nullable;

import com.pmease.commons.bootstrap.Lifecycle;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;

public interface BranchManager extends Lifecycle {

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

	public @Nullable Branch findBy(String branchFQN);
	
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
     * corresponding git repository. This method will proceed even if there are 
     * open pull requests associating with the branch. This is designed to be so 
     * in order to make sure branch deletion always succeeds as otherwise repository
     * sanity check might encounter difficulties if the branch ref does not exist 
     * in Git.
     * 
     * @param branch
     * 			branch to be deleted
     */
    public void delete(Branch branch);
    
    public void save(Branch branch);
    
    public void trim(Collection<Long> branchIds);
    
}
