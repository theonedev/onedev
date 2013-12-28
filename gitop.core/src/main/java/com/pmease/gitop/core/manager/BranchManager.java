package com.pmease.gitop.core.manager;

import javax.annotation.Nullable;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.gitop.core.manager.impl.DefaultBranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;

@ImplementedBy(DefaultBranchManager.class)
public interface BranchManager extends GenericDao<Branch> {

	/**
	 * Find branch by project and branch name.
	 * 
	 * @param project
	 * 			project to find branch inside
	 * @param branchName
	 * 			name of the branch to find
	 * @return
	 * 			found branch, or <tt>null</tt> if not found
	 */
	public @Nullable Branch findBy(Project project, String branchName);
	
	/**
	 * Find default branch in specified project.
	 * 
	 * @param project
	 * 			project to find default branch
	 * @return
	 * 			found default branch, or <tt>null</tt> if default branch 
	 * 			can not be found
	 */
    public @Nullable Branch findDefault(Project project);
    
    /**
     * Sync branch information of specified project in database with git repository.
     * 
     * @param project
     * 			branches of the project to be synced
     */
    public void syncWithGit(Project project);
    
    /**
     * Delete all refs pointed to this branch and its associated pull requests and 
     * pull request updates.
     * 
     * @param branch
     * 			branch whose refs, pull request refs, and pull request update refs 
     * 			to be deleted
     */
    public void deleteRefs(Branch branch);
}
