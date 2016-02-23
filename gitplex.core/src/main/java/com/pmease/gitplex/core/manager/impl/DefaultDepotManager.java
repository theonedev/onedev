package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pmease.commons.git.Git;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.listeners.LifecycleListener;
import com.pmease.gitplex.core.listeners.DepotListener;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

@Singleton
public class DefaultDepotManager implements DepotManager, LifecycleListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDepotManager.class);
	
	private final Dao dao;
	
	private final Provider<Set<DepotListener>> listenersProvider;
	
    private final StorageManager storageManager;
    
    private final UserManager userManager;
    
    private final AuxiliaryManager auxiliaryManager;
   
    private final PullRequestManager pullRequestManager;
    
    private final String gitUpdateHook;
    
    private final String gitPostReceiveHook;
    
	private final BiMap<Pair<Long, String>, Long> nameToId = HashBiMap.create();
	
	private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    
    @Inject
    public DefaultDepotManager(Dao dao, UserManager userManager, StorageManager storageManager,
    		AuxiliaryManager auxiliaryManager, PullRequestManager pullRequestManager, 
    		Provider<Set<DepotListener>> listenersProvider) {
    	this.dao = dao;
        this.storageManager = storageManager;
        this.userManager = userManager;
        this.auxiliaryManager = auxiliaryManager;
        this.pullRequestManager = pullRequestManager;
        this.listenersProvider = listenersProvider;
        
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
    public void save(final Depot depot) {
    	dao.persist(depot);
    	
        checkSanity(depot);
        
        dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(depot.getId(), new Pair<>(depot.getUser().getId(), depot.getName()));
				} finally {
					idLock.writeLock().unlock();
				}
			}
        	
        });
    }

    @Transactional
    @Override
    public void delete(final Depot depot) {
		for (DepotListener listener: listenersProvider.get())
			listener.beforeDelete(depot);
		
    	Query query = dao.getSession().createQuery("update Depot set forkedFrom=null where forkedFrom=:forkedFrom");
    	query.setParameter("forkedFrom", depot);
    	query.executeUpdate();
    	
        dao.remove(depot);

		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().remove(depot.getId());
				} finally {
					idLock.writeLock().unlock();
				}
		        FileUtils.deleteDir(storageManager.getDepotDir(depot));
			}
			
		});
		
    }
    
    @Sessional
    @Override
    public Depot findBy(String ownerName, String depotName) {
    	User user = userManager.findByName(ownerName);
    	if (user != null)
    		return findBy(user, depotName);
    	else
    		return null;
    }

    @Sessional
    @Override
    public Depot findBy(User owner, String depotName) {
    	idLock.readLock().lock();
    	try {
    		Long id = nameToId.get(new Pair<>(owner.getId(), depotName));
    		if (id != null)
    			return dao.load(Depot.class, id);
    		else
    			return null;
    	} finally {
    		idLock.readLock().unlock();
    	}
    }

    @Sessional
    @Override
    public Depot findBy(String depotFQN) {
    	String userName = StringUtils.substringBefore(depotFQN, Depot.FQN_SEPARATOR);
    	User user = userManager.findByName(userName);
    	if (user != null)
    		return findBy(user, StringUtils.substringAfter(depotFQN, Depot.FQN_SEPARATOR));
    	else
    		return null;
    }

    @Transactional
	@Override
	public Depot fork(Depot depot, User user) {
		if (depot.getOwner().equals(user))
			return depot;
		
		Depot forked = null;
		for (Depot each: user.getDepots()) {
			if (depot.equals(each.getForkedFrom())) {
				forked = each;
				break;
			}
		}
		if (forked == null) {
			Set<String> existingNames = new HashSet<>();
			for (Depot each: user.getDepots()) 
				existingNames.add(each.getName());
			
			forked = new Depot();
			forked.setOwner(user);
			forked.setForkedFrom(depot);
			if (existingNames.contains(depot.getName())) {
				int suffix = 1;
				while (existingNames.contains(depot.getName() + "_" + suffix))
					suffix++;
				forked.setName(depot.getName() + "_" + suffix);
			} else {
				forked.setName(depot.getName());
			}

			dao.persist(forked);

            FileUtils.cleanDir(forked.git().depotDir());
            forked.git().clone(depot.git().depotDir().getAbsolutePath(), true, false, false, null);
            
            checkSanity(forked);
		}
		
		return forked;
	}

	@Transactional
	@Override
	public void checkSanity(Depot depot) {
		logger.debug("Checking sanity of repository '{}'...", depot);

		Git git = depot.git();

		if (git.depotDir().exists() && !git.isValid()) {
        	logger.warn("Directory '" + git.depotDir() + "' is not a valid git repository, removing...");
        	FileUtils.deleteDir(git.depotDir());
        }
        
        if (!git.depotDir().exists()) {
        	logger.warn("Initializing git repository in '" + git.depotDir() + "'...");
            FileUtils.createDir(git.depotDir());
            git.init(true);
        }
        
        if (!depot.isValid()) {
            File hooksDir = new File(depot.git().depotDir(), "hooks");

            File gitUpdateHookFile = new File(hooksDir, "update");
            FileUtils.writeFile(gitUpdateHookFile, gitUpdateHook);
            gitUpdateHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, gitPostReceiveHook);
            gitPostReceiveHookFile.setExecutable(true);
        }
        
        auxiliaryManager.collect(depot);
	}

	@Sessional
	@Override
	public void systemStarting() {
        for (Depot depot: dao.allOf(Depot.class)) 
        	nameToId.inverse().put(depot.getId(), new Pair<>(depot.getUser().getId(), depot.getName()));
	}
	
	@Transactional
	@Override
	public void checkSanity() {
		logger.info("Checking sanity of repositories...");
		for (Depot depot: dao.query(EntityCriteria.of(Depot.class), 0, 0))
			checkSanity(depot);
	}

	@Transactional
	@Override
	public void systemStarted() {
		checkSanity();
		pullRequestManager.checkSanity();
	}

	@Override
	public void systemStopping() {
	}

	@Override
	public void systemStopped() {
	}

}
