package com.pmease.gitop.core.manager.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;

@Singleton
public class DefaultBranchManager extends AbstractGenericDao<Branch> implements BranchManager {
	
	private final PullRequestManager pullRequestManager;

	@Inject
	public DefaultBranchManager(GeneralDao generalDao, PullRequestManager pullRequestManager) {
		super(generalDao);
		this.pullRequestManager = pullRequestManager;
	}

    @Sessional
    @Override
    public Branch findBy(Project project, String name) {
        return find(new Criterion[]{Restrictions.eq("project", project), Restrictions.eq("name", name)});
    }

    @Sessional
	@Override
	public Branch findDefault(Project project) {
		return findBy(project, project.resolveDefaultBranchName());
	}

    @Transactional
	@Override
	public void delete(Branch branch) {
    	deleteRefs(branch);
		super.delete(branch);
	}
    
    @Sessional
    @Override
    public void deleteRefs(Branch branch) {
		for (PullRequest request: branch.getIngoingRequests())
			pullRequestManager.deleteRefs(request);
		for (PullRequest request: branch.getOutgoingRequests())
			pullRequestManager.deleteRefs(request);
		branch.getProject().code().deleteBranch(branch.getName());
    }
    
	@Transactional
	@Override
	public void syncWithGit(Project project) {
		Collection<String> branchesInGit = project.code().listBranches();
		for (Branch branch: project.getBranches()) {
			if (!branchesInGit.contains(branch.getName()))
				delete(branch);
		}
		
		for (String branchInGit: branchesInGit) {
			boolean found = false;
			for (Branch branch: project.getBranches()) {
				if (branch.getName().equals(branchInGit)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Branch branch = new Branch();
				branch.setName(branchInGit);
				branch.setProject(project);
				save(branch);
			}
		}
		
		String defaultBranchName = project.resolveDefaultBranchName();
		if (!branchesInGit.isEmpty() && !branchesInGit.contains(defaultBranchName)) {
			if (!branchesInGit.contains("master"))
				defaultBranchName = "master";
			else
				defaultBranchName = branchesInGit.iterator().next();
			project.code().updateSymbolicRef("HEAD", Git.REFS_HEADS + defaultBranchName, null);
		}
	}

}
