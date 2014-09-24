package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.StorageManager;

import org.apache.commons.io.IOUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultRepositoryManager implements RepositoryManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRepositoryManager.class);

	private final Dao dao;
	
    private final BranchManager branchManager;
    
    private final StorageManager storageManager;
    
    private final String gitUpdateHook;
    
    private final String gitPostReceiveHook;
    
    @Inject
    public DefaultRepositoryManager(Dao dao, BranchManager branchManager, StorageManager storageManager) {
    	this.dao = dao;
    	
        this.branchManager = branchManager;
        this.storageManager = storageManager;
        
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
    	dao.persist(repository);
    	
        checkSanity(repository);
    }

    @Transactional
    @Override
    public void delete(Repository repository) {
    	for (Branch branch: repository.getBranches()) {
	    	for (PullRequest request: branch.getOutgoingRequests()) {
	    		request.setSource(null);
	    		dao.persist(request);
	    	}
    	}
    	
    	for (Repository each: repository.getForks()) {
    		each.setForkedFrom(null);
    		save(each);
    	}
    	
        dao.remove(repository);

        FileUtils.deleteDir(storageManager.getRepoDir(repository));
    }

    @Sessional
    @Override
    public Repository findBy(String ownerName, String repositoryName) {
        Criteria criteria = dao.getSession().createCriteria(Repository.class);
        criteria.add(Restrictions.eq("name", repositoryName));
        criteria.createAlias("owner", "owner");
        criteria.add(Restrictions.eq("owner.name", ownerName));

        criteria.setMaxResults(1);
        return (Repository) criteria.uniqueResult();
    }

    @Sessional
    @Override
    public Repository findBy(User owner, String repositoryName) {
        return dao.find(EntityCriteria.of(Repository.class)
        		.add(Restrictions.eq("owner.id", owner.getId()))
        		.add(Restrictions.eq("name", repositoryName)));
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

			dao.persist(forked);

            FileUtils.cleanDir(forked.git().repoDir());
            forked.git().clone(repository.git().repoDir().getAbsolutePath(), true, false, false, null);
            
            checkSanity(forked);
		}
		
		return forked;
	}

	@Transactional
	@Override
	public void checkSanity() {
		for (Repository repository: dao.query(EntityCriteria.of(Repository.class), 0, 0)) {
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

		Map<String, Branch> branchesInDB = new HashMap<String, Branch>();
		Map<String, String> branchesInGit = repository.git().listBranches();
		for (Iterator<Branch> it = repository.getBranches().iterator(); it.hasNext();) {
			Branch branch = it.next();
			if (branchesInGit.containsKey(branch.getName())) {
				branchesInDB.put(branch.getName(), branch);
			} else {
				branchManager.delete(branch, null);
				it.remove();
			}
		}
		
		for (Map.Entry<String, String> entry: branchesInGit.entrySet()) {
			Branch branch = branchesInDB.get(entry.getKey());
			if (branch == null) {
				branch = new Branch();
				branch.setName(entry.getKey());
				branch.setHeadCommitHash(entry.getValue());
				branch.setRepository(repository);
				dao.persist(branch);
			} else if (!branch.getHeadCommitHash().equals(entry.getValue()))	 {
				branch.setHeadCommitHash(entry.getValue());
				branch.setUpdater(null);
				dao.persist(branch);
			}
		}
		
		String defaultBranchName = repository.git().resolveDefaultBranch();
		if (!branchesInGit.isEmpty() && !branchesInGit.containsKey(defaultBranchName))
			repository.git().updateDefaultBranch(branchesInGit.keySet().iterator().next());
	}
	
}
