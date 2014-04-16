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
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.manager.PullRequestManager;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.storage.StorageManager;

@Singleton
public class DefaultRepositoryManager extends AbstractGenericDao<Repository> implements RepositoryManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRepositoryManager.class);

    private final BranchManager branchManager;
    
    private final StorageManager storageManager;
    
    private final PullRequestManager pullRequestManager;

    private final String gitUpdateHook;
    
    private final String gitPostReceiveHook;
    
    @Inject
    public DefaultRepositoryManager(GeneralDao generalDao, BranchManager branchManager, 
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
    public void save(Repository repository) {
    	super.save(repository);
    	
        checkSanity(repository);
    }

    @Transactional
    @Override
    public void delete(Repository repository) {
    	for (Branch branch: repository.getBranches()) {
	    	for (PullRequest request: branch.getOutgoingRequests()) {
	    		request.setSource(null);
	    		pullRequestManager.save(request);
	    	}
    	}
    	
    	for (Repository each: repository.getForks()) {
    		each.setForkedFrom(null);
    		save(each);
    	}
    	
        super.delete(repository);

        storageManager.getStorage(repository).delete();
    }

    @Sessional
    @Override
    public Repository findBy(String ownerName, String repositoryName) {
        Criteria criteria = createCriteria();
        criteria.add(Restrictions.eq("name", repositoryName));
        criteria.createAlias("owner", "owner");
        criteria.add(Restrictions.eq("owner.name", ownerName));

        criteria.setMaxResults(1);
        return (Repository) criteria.uniqueResult();
    }

    @Sessional
    @Override
    public Repository findBy(User owner, String repositoryName) {
        return find(Restrictions.eq("owner.id", owner.getId()),
                Restrictions.eq("name", repositoryName));
    }

	@Transactional
	@Override
	public Repository fork(Repository repository, User user) {
		if (repository.getOwner().equals(user))
			return repository;
		
		Repository forked = null;
		for (Repository each: user.getRepositories()) {
			if (repository.equals(each.getForkedFrom())) {
				forked = each;
				break;
			}
		}
		if (forked == null) {
			Set<String> existingNames = new HashSet<>();
			for (Repository each: user.getRepositories()) 
				existingNames.add(each.getName());
			
			forked = new Repository();
			forked.setOwner(user);
			forked.setForkedFrom(repository);
			if (existingNames.contains(repository.getName())) {
				int suffix = 1;
				while (existingNames.contains(repository.getName() + "_" + suffix))
					suffix++;
				forked.setName(repository.getName() + "_" + suffix);
			} else {
				forked.setName(repository.getName());
			}

			super.save(forked);

            FileUtils.cleanDir(forked.git().repoDir());
            forked.git().clone(repository.git().repoDir().getAbsolutePath(), true);
            
            checkSanity(forked);
		}
		
		return forked;
	}

	@Transactional
	@Override
	public void checkSanity() {
		for (Repository repository: query()) {
			checkSanity(repository);
		}
	}
	
	@Transactional
	@Override
	public void checkSanity(Repository repository) {
		logger.debug("Checking sanity of repository '{}'...", repository);

		Git git = repository.git();

		if (git.repoDir().exists() && !git.isValid()) {
        	logger.warn("Directory '" + git.repoDir() + "' is not a valid git repository, removing...");
        	FileUtils.deleteDir(git.repoDir());
        }
        
        if (!git.repoDir().exists()) {
        	logger.warn("Initializing git repository in '" + git.repoDir() + "'...");
            FileUtils.createDir(git.repoDir());
            git.init(true);
        }
        
        if (!Repository.isValid(git)) {
            File hooksDir = new File(repository.git().repoDir(), "hooks");

            File gitUpdateHookFile = new File(hooksDir, "update");
            FileUtils.writeFile(gitUpdateHookFile, gitUpdateHook);
            gitUpdateHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, gitPostReceiveHook);
            gitPostReceiveHookFile.setExecutable(true);
        }
		
		logger.debug("Syncing branches of repository '{}'...", repository);
		
		Collection<String> branchesInGit = repository.git().listBranches();
		for (Branch branch: repository.getBranches()) {
			if (!branchesInGit.contains(branch.getName()))
				branchManager.delete(branch);
		}
		
		for (String branchInGit: branchesInGit) {
			boolean found = false;
			for (Branch branch: repository.getBranches()) {
				if (branch.getName().equals(branchInGit)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Branch branch = new Branch();
				branch.setName(branchInGit);
				branch.setRepository(repository);
				branchManager.save(branch);
			}
		}
		
		String defaultBranchName = repository.git().resolveDefaultBranch();
		if (!branchesInGit.isEmpty() && !branchesInGit.contains(defaultBranchName))
			repository.git().updateDefaultBranch(branchesInGit.iterator().next());
	}
}
