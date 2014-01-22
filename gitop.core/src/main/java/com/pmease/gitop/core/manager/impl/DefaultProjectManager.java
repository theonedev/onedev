package com.pmease.gitop.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractGenericDao;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitop.core.manager.BranchManager;
import com.pmease.gitop.core.manager.ProjectManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Project;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.storage.StorageManager;

@Singleton
public class DefaultProjectManager extends AbstractGenericDao<Project> implements ProjectManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultProjectManager.class);

    private final BranchManager branchManager;
    
    private final StorageManager storageManager;
    
    private final PullRequestManager pullRequestManager;

    private final String gitUpdateHook;
    
    private final String gitPostReceiveHook;
    
    @Inject
    public DefaultProjectManager(GeneralDao generalDao, BranchManager branchManager, 
    		StorageManager storageManager, PullRequestManager pullRequestManager) {
        super(generalDao);

        this.branchManager = branchManager;
        this.storageManager = storageManager;
        this.pullRequestManager = pullRequestManager;
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-update-hook")) {
        	Preconditions.checkNotNull(is);
            gitUpdateHook = StringUtils.join(IOUtils.readLines(is), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-postreceive-hook")) {
        	Preconditions.checkNotNull(is);
            gitPostReceiveHook = StringUtils.join(IOUtils.readLines(is), "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Transactional
    @Override
    public void save(Project project) {
    	super.save(project);
    	
        checkSanity(project);
    }

    private void setupHooks(Project project) {
        File hooksDir = new File(project.code().repoDir(), "hooks");

        File gitUpdateHookFile = new File(hooksDir, "update");
        FileUtils.writeFile(gitUpdateHookFile, gitUpdateHook);
        gitUpdateHookFile.setExecutable(true);
        
        File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
        FileUtils.writeFile(gitPostReceiveHookFile, gitPostReceiveHook);
        gitPostReceiveHookFile.setExecutable(true);
    }
    
    @Transactional
    @Override
    public void delete(Project project) {
    	for (Branch branch: project.getBranches()) {
	    	for (PullRequest request: branch.getOutgoingRequests()) {
	    		request.setSource(null);
	    		pullRequestManager.save(request);
	    	}
    	}
    	
    	for (Project each: project.getForks()) {
    		each.setForkedFrom(null);
    		save(each);
    	}
    	
        super.delete(project);

        storageManager.getStorage(project).delete();
    }

    @Sessional
    @Override
    public Project findBy(String ownerName, String projectName) {
        Criteria criteria = createCriteria();
        criteria.add(Restrictions.eq("name", projectName));
        criteria.createAlias("owner", "owner");
        criteria.add(Restrictions.eq("owner.name", ownerName));

        criteria.setMaxResults(1);
        return (Project) criteria.uniqueResult();
    }

    @Sessional
    @Override
    public Project findBy(User owner, String projectName) {
        return find(Restrictions.eq("owner.id", owner.getId()),
                Restrictions.eq("name", projectName));
    }

	@Transactional
	@Override
	public Project fork(Project project, User user) {
		if (project.getOwner().equals(user))
			return project;
		
		Project forked = null;
		for (Project each: user.getProjects()) {
			if (project.equals(each.getForkedFrom())) {
				forked = each;
				break;
			}
		}
		if (forked == null) {
			Set<String> existingNames = new HashSet<>();
			for (Project each: user.getProjects()) 
				existingNames.add(each.getName());
			
			forked = new Project();
			forked.setOwner(user);
			forked.setForkedFrom(project);
			if (existingNames.contains(project.getName())) {
				int suffix = 1;
				while (existingNames.contains(project.getName() + "_" + suffix))
					suffix++;
				forked.setName(project.getName() + "_" + suffix);
			} else {
				forked.setName(project.getName());
			}

			super.save(forked);

            FileUtils.cleanDir(forked.code().repoDir());
            forked.code().clone(project.code().repoDir().getAbsolutePath(), true);
            setupHooks(forked);
            
            checkSanity(forked);
		}
		
		return forked;
	}

	@Transactional
	@Override
	public void checkSanity() {
		for (Project project: query()) {
			checkSanity(project);
		}
	}
	
	@Transactional
	@Override
	public void checkSanity(Project project) {
		logger.debug("Checking sanity of project '{}'...", project);
        File codeDir = project.code().repoDir();
        if (codeDir.exists() && !Project.isCode(new Git(codeDir))) {
        	logger.warn("Directory '" + codeDir + "' is not a valid gitop repository, removing...");
        	FileUtils.deleteDir(codeDir);
        }
        
        if (!codeDir.exists()) {
        	logger.warn("Initializing gitop repository in '" + codeDir + "'...");
            FileUtils.createDir(codeDir);
            new Git(codeDir).init(true);
            setupHooks(project);
        }
		
		logger.debug("Syncing branches of project '{}'...", project);
		
		Collection<String> branchesInGit = project.code().listBranches();
		for (Branch branch: project.getBranches()) {
			if (!branchesInGit.contains(branch.getName()))
				branchManager.delete(branch);
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
				branchManager.save(branch);
			}
		}
		
		String defaultBranchName = project.code().resolveDefaultBranch();
		if (!branchesInGit.isEmpty() && !branchesInGit.contains(defaultBranchName))
			project.code().updateDefaultBranch(branchesInGit.iterator().next());
	}
}
