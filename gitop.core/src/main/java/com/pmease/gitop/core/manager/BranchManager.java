package com.pmease.gitop.core.manager;

import java.util.Collection;

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
     * Save/update specified branch record in database. Note that this won't update the git 
     * repository.
     * 
     * @param branch
     * 			branch to be saved
     */
    public void save(Branch branch);

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
    
    public void trim(Collection<Long> branchIds);
    
    /**
     * Sync branch information of specified project between database and git repository.
     *  
     * @param project
     * 			project whose branch information should be synced
     */
	public void syncBranches(Project project);
	
    /**
     * Sync branch information of all projects in the system.
     * 
     */
	public void syncBranches();
}
