package com.pmease.gitop.core.manager;

import com.google.inject.ImplementedBy;
import com.pmease.commons.persistence.dao.GenericDao;
import com.pmease.commons.util.namedentity.EntityLoader;
import com.pmease.gitop.core.manager.impl.DefaultBranchManager;
import com.pmease.gitop.core.model.Branch;
import com.pmease.gitop.core.model.Repository;

@ImplementedBy(DefaultBranchManager.class)
public interface BranchManager extends GenericDao<Branch> {

	public Branch lookupBranch(Repository repository, String branchName);
	
	public EntityLoader asEntityLoader(Repository repository);
	
}
