package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.UnitOfWork;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.listeners.LifecycleListener;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;

@Singleton
public class DefaultBranchManager implements BranchManager, LifecycleListener {
	
	private final Dao dao;
	
	private final PullRequestManager pullRequestManager;
	
	private final UserManager userManager;
	
	private final RepositoryManager repositoryManager;
	
	private final UnitOfWork unitOfWork;
	
	private final BiMap<Pair<Long, String>, Long> nameToId = HashBiMap.create();
	
	private final ReadWriteLock idLock = new ReentrantReadWriteLock();

	@Inject
	public DefaultBranchManager(Dao dao, PullRequestManager pullRequestManager, UserManager userManager,
			RepositoryManager repositoryManager, UnitOfWork unitOfWork) {
		this.dao = dao;
		this.pullRequestManager = pullRequestManager;
		this.userManager = userManager;
		this.repositoryManager = repositoryManager;
		this.unitOfWork = unitOfWork;
	}

    @Sessional
    @Override
    public Branch findBy(Repository repository, String branchName) {
    	idLock.readLock().lock();
    	try {
    		Long id = nameToId.get(new Pair<>(repository.getId(), branchName));
    		if (id != null)
    			return dao.load(Branch.class, id);
    		else
    			return null;
    	} finally {
    		idLock.readLock().unlock();
    	}
    }

    @Sessional
    @Override
    public Branch findBy(String branchFQN) {
    	String repositoryName = StringUtils.substringBeforeLast(branchFQN, "/");
    	Repository repository = repositoryManager.findBy(repositoryName);
    	if (repository != null)
    		return findBy(repository, StringUtils.substringAfterLast(branchFQN, "/"));
    	else
    		return null;
    }

    @Transactional
	@Override
	public void delete(final Branch branch) {
    	for (PullRequest request: branch.getIncomingRequests()) 
    		pullRequestManager.delete(request);
    	
    	for (PullRequest request: branch.getOutgoingRequests()) {
    		if (request.isOpen())
    			pullRequestManager.discard(request, "Source branch is deleted.");
    		request.setSourceFQN(branch.getFQN());
    		request.setSource(null);
    		dao.persist(request);
    	}
		dao.remove(branch);
		
    	deleteRefs(branch);
    	
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().remove(branch.getId());
				} finally {
					idLock.writeLock().unlock();
				}
			}
			
		});
	}
    
    @Sessional
    @Override
    public void deleteRefs(Branch branch) {
		branch.getRepository().git().deleteBranch(branch.getName());
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
		branch.setLastUpdater(userManager.getCurrent());
		
		dao.persist(branch);

		/**
		 * Source branch update is key to the logic as it has to create 
		 * pull request update, so we should not postpone it to be executed
		 * in a executor service like target branch update below
		 */
		for (PullRequest request: branch.getOutgoingRequests()) {
			if (request.isOpen())
				pullRequestManager.onSourceBranchUpdate(request, true);
		}
		
		final Long branchId = branch.getId();
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(branch.getId(), new Pair<>(branch.getRepository().getId(), branch.getName()));
				} finally {
					idLock.writeLock().unlock();
				}
				
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

	@Sessional
	@Override
	public void systemStarting() {
        for (Branch branch: dao.allOf(Branch.class)) 
        	nameToId.inverse().put(branch.getId(), new Pair<>(branch.getRepository().getId(), branch.getName()));
	}

	@Override
	public void systemStarted() {
	}

	@Override
	public void systemStopping() {
	}

	@Override
	public void systemStopped() {
	}

}
