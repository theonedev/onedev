package com.pmease.gitop.core.manager;

import java.util.Collection;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.gitop.core.manager.impl.DefaultBranchManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.Project;

@ImplementedBy(DefaultBranchManager.class)
public interface BranchManager extends GenericDao<Branch> {

	public Branch find(Project project, String branchName);
	
    public Branch find(Project project, String branchName, boolean createIfNotExist);
    
    /**
     * Find all branches of specified project from repository. This method will add/remove
     * branches in database to make sure they match with branches stored in repository.
     * 
     * @param project
     *          project to find branches of
     * @return
     *          collection of found branches
     */
    public Collection<Branch> findBranches(Project project);

    public EntityLoader asEntityLoader(Project project);
	
}
