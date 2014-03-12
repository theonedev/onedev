package com.pmease.gitop.core.manager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.core.manager.PullRequestUpdateManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;

@Singleton
public class DefaultBranchManager extends AbstractGenericDao<Branch> implements BranchManager {
	
	private final PullRequestManager pullRequestManager;
	
	private final PullRequestUpdateManager pullRequestUpdateManager;
	
	@Inject
	public DefaultBranchManager(GeneralDao generalDao, 
			PullRequestManager pullRequestManager, 
			PullRequestUpdateManager pullRequestUpdateManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
		this.pullRequestUpdateManager = pullRequestUpdateManager;
	}

    @Sessional
    @Override
    public Branch findBy(Project project, String name) {
        return find(new Criterion[]{Restrictions.eq("project", project), Restrictions.eq("name", name)});
    }

    @Sessional
	@Override
	public Branch findDefault(Project project) {
		return findBy(project, project.code().resolveDefaultBranch());
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
    		pullRequestManager.save(request);
    	}
		super.delete(branch);
	}
    
    @Sessional
    @Override
    public void deleteRefs(Branch branch) {
		branch.getProject().code().deleteBranch(branch.getName());
    }

    @Transactional
	@Override
	public void create(final Branch branch, final String commitHash) {
		save(branch);

		getSession().getTransaction().registerSynchronization(new Synchronization() {

			public void afterCompletion(int status) {
				if (status == Status.STATUS_COMMITTED) { 
					branch.getProject().code().createBranch(branch.getName(), commitHash);
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
    	
		save(branch);

		getSession().getTransaction().registerSynchronization(new Synchronization() {

			public void afterCompletion(int status) {
				if (status == Status.STATUS_COMMITTED)  
					branch.getProject().code().renameBranch(oldName, branch.getName());
			}

			public void beforeCompletion() {
				
			}
			
		});
	}

	@Override
	public void trim(Collection<Long> branchIds) {
		for (Iterator<Long> it = branchIds.iterator(); it.hasNext();) {
			if (get(it.next()) == null)
				it.remove();
		}
	}

	@Transactional
	@Override
	public void onBranchRefUpdate(Branch branch, User user) {
		for (PullRequest request: branch.getIncomingRequests()) {
			if (request.isOpen())
				pullRequestManager.refresh(request);
		}
		
		// Calculates most recent open pull request for each branch. Note that although 
		// we do not allow multiple opened requests for a single branch, there still 
		// exist some chances multiple requests are opening for same branch, so we need 
		// to handle this case here.
		Map<Branch, PullRequest> branchRequests = new HashMap<>();
		for (PullRequest request: branch.getOutgoingRequests()) {
			if (request.isOpen()) {
				PullRequest branchRequest = branchRequests.get(request.getTarget());
				if (branchRequest == null)
					branchRequests.put(request.getTarget(), request);
				else if (request.getId() > branchRequest.getId())
					branchRequests.put(request.getTarget(), request);
			}
		}
		
		for (PullRequest request: branchRequests.values()) {
			pullRequestUpdateManager.update(request, user);
			pullRequestManager.refresh(request);
		}		
	}

}
