package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultBranchManager implements BranchManager {
	
	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	private final RepositoryManager repositoryManager;
	
	private final UnitOfWork unitOfWork;
	
	@Inject
	public DefaultBranchManager(Dao dao, PullRequestManager pullRequestManager, 
			RepositoryManager repositoryManager, UnitOfWork unitOfWork) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
		this.repositoryManager = repositoryManager;
		this.unitOfWork = unitOfWork;
	}

    @Sessional
    @Override
    public Branch findBy(Repository repository, String name) {
        return dao.find(EntityCriteria.of(Branch.class)
        		.add(Restrictions.eq("repository", repository))
        		.add(Restrictions.eq("name", name)));
    }

    @Sessional
    @Override
    public Branch findBy(String branchPath) {
    	String repositoryName = StringUtils.substringBeforeLast(branchPath, "/");
    	Repository repository = repositoryManager.findBy(repositoryName);
    	if (repository != null)
    		return findBy(repository, StringUtils.substringAfterLast(branchPath, "/"));
    	else
    		return null;
    }

    @Transactional
	@Override
	public void delete(Branch branch) {
    	deleteRefs(branch);
    	
    	for (PullRequest request: branch.getIncomingRequests()) { 
    		pullRequestManager.delete(request);
    	}
    	
    	for (PullRequest request: branch.getOutgoingRequests()) {
    		request.setSource(null);
    		dao.persist(request);
    		pullRequestManager.discard(request, null, "Source branch is deleted.");
    	}
		dao.remove(branch);
	}
    
    @Sessional
    @Override
    public void deleteRefs(Branch branch) {
		branch.getRepository().git().deleteBranch(branch.getName());
    }

    @Transactional
	@Override
	public void create(final Branch branch, final String commitHash) {
		dao.persist(branch);
		branch.getRepository().git().createBranch(branch.getName(), commitHash);
	}

	@Override
	public void trim(Collection<Long> branchIds) {
		for (Iterator<Long> it = branchIds.iterator(); it.hasNext();) {
			if (dao.get(Branch.class, it.next()) == null)
				it.remove();
		}
	}

	@Transactional
	@Override
	public void save(Branch branch) {
		dao.persist(branch);

		for (PullRequest request: branch.getOutgoingRequests()) {
			if (request.isOpen())
				pullRequestManager.onSourceBranchUpdate(request);
		}
		
		final Long branchId = branch.getId();
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.asyncCall(new Runnable() {

					@Override
					public void run() {
						Branch branch = dao.load(Branch.class, branchId);
						
						for (PullRequest request: branch.getIncomingRequests()) {
							if (request.isOpen()) 
								pullRequestManager.onTargetBranchUpdate(request);
						}
					}
					
				});
			}
			
		});
	}

}
