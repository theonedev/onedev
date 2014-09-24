package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.PullRequestUpdateManager;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultBranchManager implements BranchManager {
	
	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	private final Executor executor;
	
	private final UnitOfWork unitOfWork;
	
	@Inject
	public DefaultBranchManager(Dao dao, PullRequestManager pullRequestManager, 
			PullRequestUpdateManager pullRequestUpdateManager, Executor executor, 
			UnitOfWork unitOfWork) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
		this.executor = executor;
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
	public Branch findDefault(Repository repository) {
		return findBy(repository, repository.git().resolveDefaultBranch());
	}

    @Transactional
	@Override
	public void delete(Branch branch, User user) {
    	deleteRefs(branch);
    	
    	for (PullRequest request: branch.getIncomingRequests()) { 
    		pullRequestManager.delete(request);
    	}
    	
    	for (PullRequest request: branch.getOutgoingRequests()) {
    		pullRequestManager.discard(request, user, "Source branch is deleted.");
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
	public void save(final Branch branch) {
		dao.persist(branch);
		
		dao.getSession().getTransaction().registerSynchronization(new Synchronization() {
			
			@Override
			public void beforeCompletion() {
			}
			
			@Override
			public void afterCompletion(int status) {
				if (status == Status.STATUS_COMMITTED) {
					final Long branchId = branch.getId();
					executor.execute(new Runnable() {

						@Override
						public void run() {
							unitOfWork.begin();
							try {
								Branch branch = dao.load(Branch.class, branchId);
								for (PullRequest each: branch.getIncomingRequests()) {
									if (each.isOpen())
										pullRequestManager.refresh(each);
								}
								
								// Calculates most recent open pull request for each branch. Note that although 
								// we do not allow multiple opened requests for a single branch, there still 
								// exist some chances multiple requests are opening for same branch, so we need 
								// to handle this case here.
								Map<Branch, PullRequest> branchRequests = new HashMap<>();
								for (PullRequest each: branch.getOutgoingRequests()) {
									if (each.isOpen()) {
										PullRequest branchRequest = branchRequests.get(each.getTarget());
										if (branchRequest == null)
											branchRequests.put(each.getTarget(), each);
										else if (each.getId() > branchRequest.getId())
											branchRequests.put(each.getTarget(), each);
									}
								}
								
								for (PullRequest each: branchRequests.values()) {
									PullRequestUpdate update = new PullRequestUpdate();
									update.setRequest(each);
									update.setUser(branch.getUpdater());
									update.setDate(new Date());
									update.setHeadCommitHash(branch.getHeadCommitHash());
									
									each.getUpdates().add(update);
									
									pullRequestUpdateManager.save(update);
	
									each.setUpdateDate(new Date());
									pullRequestManager.refresh(each);
								}	
							} finally {
								unitOfWork.end();
							}
						}
						
					});
				}
			}
			
		});
	}

}
