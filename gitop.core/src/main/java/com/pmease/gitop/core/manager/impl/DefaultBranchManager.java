package com.pmease.gitop.core.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;

@Singleton
public class DefaultBranchManager implements BranchManager {
	
	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	@Inject
	public DefaultBranchManager(Dao dao, PullRequestManager pullRequestManager, 
			PullRequestUpdateManager pullRequestUpdateManager) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
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
	public Branch findDefault(Repository repository) {
		return findBy(repository, repository.git().resolveDefaultBranch());
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

		dao.getSession().getTransaction().registerSynchronization(new Synchronization() {

			public void afterCompletion(int status) {
				if (status == Status.STATUS_COMMITTED) { 
					branch.getRepository().git().createBranch(branch.getName(), commitHash);
				}
			}

			public void beforeCompletion() {
				
			}
			
		});
		
	}

    @Transactional
	@Override
	public void rename(final Branch branch, String newName) {
    	final String oldName = branch.getName(); 
    	branch.setName(newName);
    	
		dao.persist(branch);

		dao.getSession().getTransaction().registerSynchronization(new Synchronization() {

			public void afterCompletion(int status) {
				if (status == Status.STATUS_COMMITTED)  
					branch.getRepository().git().renameBranch(oldName, branch.getName());
			}

			public void beforeCompletion() {
				
			}
			
		});
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
	public void onBranchRefUpdate(Branch branch, User byUser, PullRequest byRequest) {
		for (PullRequest request: branch.getIncomingRequests()) {
			if (request.isOpen() && !request.equals(byRequest))
				pullRequestManager.refresh(request);
		}
		
		// Calculates most recent open pull request for each branch. Note that although 
		// we do not allow multiple opened requests for a single branch, there still 
		// exist some chances multiple requests are opening for same branch, so we need 
		// to handle this case here.
		Map<Branch, PullRequest> branchRequests = new HashMap<>();
		for (PullRequest request: branch.getOutgoingRequests()) {
			if (request.isOpen() && !request.equals(byRequest)) {
				PullRequest branchRequest = branchRequests.get(request.getTarget());
				if (branchRequest == null)
					branchRequests.put(request.getTarget(), request);
				else if (request.getId() > branchRequest.getId())
					branchRequests.put(request.getTarget(), request);
			}
		}
		
		for (PullRequest request: branchRequests.values()) {
			PullRequestUpdate update = new PullRequestUpdate();
			request.getUpdates().add(update);
			update.setRequest(request);
			update.setUser(byUser);
			
			request.getUpdates().add(update);
			update.setHeadCommit(branch.getHeadCommit());
			
			pullRequestUpdateManager.save(update);

			request.setUpdateDate(new Date());
			pullRequestManager.refresh(request);
		}		
	}

}
