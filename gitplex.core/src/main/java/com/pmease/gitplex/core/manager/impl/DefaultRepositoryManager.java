package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.git.BriefCommit;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.events.RepositoryRemoved;
import com.pmease.gitplex.core.events.SystemStarted;
import com.pmease.gitplex.core.events.SystemStarting;
import com.pmease.gitplex.core.manager.BranchManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultRepositoryManager implements RepositoryManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRepositoryManager.class);

	private final Dao dao;
	
	private final EventBus eventBus;
	
    private final BranchManager branchManager;
    
    private final StorageManager storageManager;
    
    private final UserManager userManager;
    
    private final String gitUpdateHook;
    
    private final String gitPostReceiveHook;
    
	private final BiMap<Pair<Long, String>, Long> nameToId = HashBiMap.create();
	
	private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    
    @Inject
    public DefaultRepositoryManager(EventBus eventBus, Dao dao, BranchManager branchManager, 
    		UserManager userManager, StorageManager storageManager) {
    	this.eventBus = eventBus;
    	this.dao = dao;
        this.branchManager = branchManager;
        this.storageManager = storageManager;
        this.userManager = userManager;
        
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
    public void save(final Repository repository) {
    	dao.persist(repository);
    	
        checkSanity(repository);
        
        dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(repository.getId(), new Pair<>(repository.getUser().getId(), repository.getName()));
				} finally {
					idLock.writeLock().unlock();
				}
			}
        	
        });
    }

    @Transactional
    @Override
    public void delete(final Repository repository) {
    	for (Branch branch: repository.getBranches())
	    	branchManager.delete(branch);
    	
    	for (Repository each: repository.getForks()) {
    		each.setForkedFrom(null);
    		save(each);
    	}
    	
        dao.remove(repository);

        FileUtils.deleteDir(storageManager.getRepoDir(repository));
        
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().remove(repository.getId());
				} finally {
					idLock.writeLock().unlock();
				}
			}
			
		});
		
		eventBus.post(new RepositoryRemoved(repository));
    }

    @Sessional
    @Override
    public Repository findBy(String ownerName, String repositoryName) {
    	User user = userManager.findByName(ownerName);
    	if (user != null)
    		return findBy(user, repositoryName);
    	else
    		return null;
    }

    @Sessional
    @Override
    public Repository findBy(User owner, String repositoryName) {
    	idLock.readLock().lock();
    	try {
    		Long id = nameToId.get(new Pair<>(owner.getId(), repositoryName));
    		if (id != null)
    			return dao.load(Repository.class, id);
    		else
    			return null;
    	} finally {
    		idLock.readLock().unlock();
    	}
    }

    @Sessional
    @Override
    public Repository findBy(String repositoryFQN) {
    	String userName = StringUtils.substringBefore(repositoryFQN, "/");
    	User user = userManager.findByName(userName);
    	if (user != null)
    		return findBy(user, StringUtils.substringAfter(repositoryFQN, "/"));
    	else
    		return null;
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
		Map<String, BriefCommit> branchesInGit = repository.git().listHeadCommits();
		for (Iterator<Branch> it = repository.getBranches().iterator(); it.hasNext();) {
			Branch branch = it.next();
			if (branchesInGit.containsKey(branch.getName())) {
				branchesInDB.put(branch.getName(), branch);
			} else {
				branchManager.delete(branch);
				it.remove();
			}
		}
		
		for (Map.Entry<String, BriefCommit> entry: branchesInGit.entrySet()) {
			Branch branch = branchesInDB.get(entry.getKey());
			if (branch == null) {
				branch = new Branch();
				branch.setName(entry.getKey());
				branch.setHeadCommitHash(entry.getValue().getHash());
				branch.setRepository(repository);
				repository.getBranches().add(branch);
				branchManager.save(branch);
			} else if (!branch.getHeadCommitHash().equals(entry.getValue().getHash()))	 {
				branch.setHeadCommitHash(entry.getValue().getHash());
				branchManager.save(branch);
			}
		}
		
		String defaultBranchName = repository.git().resolveDefaultBranch();
		if (!branchesInGit.isEmpty() && !branchesInGit.containsKey(defaultBranchName)) {
			defaultBranchName = branchesInGit.keySet().iterator().next();
			repository.git().updateDefaultBranch(defaultBranchName);
		}
		
		for (Branch branch: repository.getBranches()) {
			if (branch.getName().equals(defaultBranchName)) {
				if (!branch.isDefault()) {
					branch.setDefault(true);
					branchManager.save(branch);
				}
			} else if (branch.isDefault()) {
				branch.setDefault(false);
				branchManager.save(branch);
			}
		}
	}

	@Subscribe
	@Sessional
	public void systemStarting(SystemStarting event) {
        for (Repository repository: dao.allOf(Repository.class)) 
        	nameToId.inverse().put(repository.getId(), new Pair<>(repository.getUser().getId(), repository.getName()));
	}

	@Subscribe
	public void systemStarted(SystemStarted event) {
		logger.info("Checking repositories...");
		
		checkSanity();
	}
	
}
