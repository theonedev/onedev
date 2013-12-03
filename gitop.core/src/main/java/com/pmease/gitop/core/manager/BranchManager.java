package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.hibernate.dao.GenericDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.gitop.core.manager.impl.DefaultBranchManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;

@ImplementedBy(DefaultBranchManager.class)
public interface BranchManager extends GenericDao<Branch> {

	public Branch findBy(Project project, String branchName);
	
    public Branch findBy(Project project, String branchName, boolean createIfNotExist);
    
    public EntityLoader asEntityLoader(Project project);
	
}
