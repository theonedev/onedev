package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.listener.DepotListener;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.listener.RefListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.AuxiliaryManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.StorageManager;
import com.pmease.gitplex.core.manager.TeamAuthorizationManager;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

@Singleton
public class DefaultDepotManager extends AbstractEntityDao<Depot> implements DepotManager, LifecycleListener, RefListener {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDepotManager.class);
	
	private final Provider<Set<DepotListener>> listenersProvider;
	
    private final StorageManager storageManager;
    
    private final AccountManager userManager;
    
    private final AuxiliaryManager auxiliaryManager;
   
    private final PullRequestManager pullRequestManager;
    
    private final TeamAuthorizationManager teamAuthorizationManager;
    
    private final String gitUpdateHook;
    
    private final String gitPostReceiveHook;
    
	private final BiMap<Pair<Long, String>, Long> nameToId = HashBiMap.create();
	
	private final ReadWriteLock idLock = new ReentrantReadWriteLock();
	
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultDepotManager(Dao dao, AccountManager userManager, 
    		TeamAuthorizationManager teamAuthorizationManager,
    		StorageManager storageManager, AuxiliaryManager auxiliaryManager, 
    		PullRequestManager pullRequestManager, Provider<Set<DepotListener>> listenersProvider) {
    	super(dao);
    	
        this.storageManager = storageManager;
        this.userManager = userManager;
        this.teamAuthorizationManager = teamAuthorizationManager;
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
    
    @Override
    public Repository getRepository(Depot depot) {
    	Repository repository = repositoryCache.get(depot.getId());
    	if (repository == null) {
    		synchronized (repositoryCache) {
    			repository = repositoryCache.get(depot.getId());
    			if (repository == null) {
    				try {
						repository = new FileRepository(depot.git().depotDir());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
    				repositoryCache.put(depot.getId(), repository);
    			}
    		}
    	}
    	return repository;
    }
    
    @Transactional
    @Override
    public void save(Depot depot, Long oldAccountId, String oldName) {
    	Preconditions.checkArgument(oldAccountId==null || oldName==null, 
    			"Can not rename and transfer depot in the same time");
    	
    	boolean isNew = depot.isNew();
    	
    	persist(depot);

    	if (oldAccountId != null && !depot.getAccount().getId().equals(oldAccountId)) {
    		Account oldAccount = userManager.load(oldAccountId);
    		for (DepotListener listener: listenersProvider.get())
    			listener.onTransferDepot(depot, oldAccount);
    		
    		for (Depot each: all()) {
    			for (IntegrationPolicy policy: each.getIntegrationPolicies()) {
    				policy.onDepotTransfer(depot, oldAccount);
    			}
    			for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
    				if (it.next().onDepotTransfer(each, depot, oldAccount))
    					it.remove();
    			}
    		}
    	}
    	if (oldName != null && !depot.getName().equals(oldName)) {
    		for (DepotListener listener: listenersProvider.get())
    			listener.onRenameDepot(depot, oldName);
    		
    		for (Depot each: all()) {
    			for (IntegrationPolicy integrationPolicy: each.getIntegrationPolicies()) {
    				integrationPolicy.onDepotRename(depot.getAccount(), oldName, depot.getName());
    			}
    			for (GateKeeper gateKeeper: each.getGateKeepers()) {
    				gateKeeper.onDepotRename(depot, oldName);
    			}
    		}
    	}

    	if (isNew) {
    		checkSanity(depot);
    	}
        
        afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(depot.getId(), new Pair<>(depot.getAccount().getId(), depot.getName()));
				} finally {
					idLock.writeLock().unlock();
				}
				if (isNew) {
					auxiliaryManager.collect(depot);
				}
			}
        	
        });
    }

    @Transactional
    @Override
    public void delete(Depot depot) {
		for (DepotListener listener: listenersProvider.get())
			listener.onDeleteDepot(depot);
		
    	Query query = getSession().createQuery("update Depot set forkedFrom=null where forkedFrom=:forkedFrom");
    	query.setParameter("forkedFrom", depot);
    	query.executeUpdate();

    	remove(depot);
    	
		for (Depot each: all()) {
			for (Iterator<IntegrationPolicy> it = each.getIntegrationPolicies().iterator(); it.hasNext();) {
				if (it.next().onDepotDelete(depot))
					it.remove();
			}
			for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onDepotDelete(depot))
					it.remove();
			}
		}

		afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().remove(depot.getId());
				} finally {
					idLock.writeLock().unlock();
				}
				getRepository(depot).close();
				repositoryCache.remove(depot.getId());
				
		        FileUtils.deleteDir(storageManager.getGitDir(depot));
			}
			
		});
		
    }
    
    @Sessional
    @Override
    public Depot findBy(String accountName, String depotName) {
    	Account user = userManager.findByName(accountName);
    	if (user != null)
    		return findBy(user, depotName);
    	else
    		return null;
    }

    @Sessional
    @Override
    public Depot findBy(Account account, String depotName) {
    	idLock.readLock().lock();
    	try {
    		Long id = nameToId.get(new Pair<>(account.getId(), depotName));
    		if (id != null)
    			return load(id);
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
    	Account user = userManager.findByName(userName);
    	if (user != null)
    		return findBy(user, StringUtils.substringAfter(depotFQN, Depot.FQN_SEPARATOR));
    	else
    		return null;
    }

    @Transactional
	@Override
	public Depot fork(Depot depot, Account user) {
		if (depot.getAccount().equals(user))
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
			forked.setAccount(user);
			forked.setForkedFrom(depot);
			if (existingNames.contains(depot.getName())) {
				int suffix = 1;
				while (existingNames.contains(depot.getName() + "_" + suffix))
					suffix++;
				forked.setName(depot.getName() + "_" + suffix);
			} else {
				forked.setName(depot.getName());
			}

			persist(forked);

            FileUtils.cleanDir(forked.git().depotDir());
            forked.git().clone(depot.git().depotDir().getAbsolutePath(), true, false, false, null);
		}
		
		return forked;
	}

	private void checkSanity(Depot depot) {
		logger.info("Checking sanity of repository '{}'...", depot);

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
	}
	
	@Sessional
	@Override
	public void systemStarting() {
        for (Depot depot: all()) 
        	nameToId.inverse().put(depot.getId(), new Pair<>(depot.getAccount().getId(), depot.getName()));
	}
	
	@Transactional
	@Override
	public void systemStarted() {
		for (Depot depot: all()) {
			checkSanity(depot);
	        auxiliaryManager.collect(depot);
		}
		
		pullRequestManager.checkSanity();
	}

	@Override
	public void systemStopping() {
		synchronized(repositoryCache) {
			for (Repository repository: repositoryCache.values()) {
				repository.close();
			}
		}
	}

	@Override
	public void systemStopped() {
	}

	@Transactional
	@Override
	public void onRefUpdate(Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (newCommit.equals(ObjectId.zeroId())) {
			String branch = GitUtils.ref2branch(refName);
			for (Depot each: all()) {
				if (branch != null) {
					for (Iterator<IntegrationPolicy> it = each.getIntegrationPolicies().iterator(); it.hasNext();) {
						if (it.next().onBranchDelete(each, depot, branch))
							it.remove();
					}
				}
				for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
					if (it.next().onRefDelete(refName))
						it.remove();
				}
			}
		}
	}

	@Sessional
	@Override
	public Collection<Depot> getAccessibles(Account account, Account user) {
		Collection<Depot> depots;
		if (account == null)
			depots = GitPlex.getInstance(DepotManager.class).all();
		else
			depots = account.getDepots();
		
		if (user == null) {
			return depots.stream().filter((depot)->depot.isPublicRead()).collect(Collectors.toSet());
		} else if (user.isAdministrator() || user.equals(account)) {
			return depots;
		} else {
			Collection<Account> adminOrganizations = user.getOrganizations()
					.stream()
					.filter((membership)->membership.isAdmin())
					.map((membership)->membership.getOrganization())
					.collect(Collectors.toSet());
			
			// return fast in special cases
			if (adminOrganizations.contains(account)) {
				return depots;
			}
			
			Collection<Account> memberOrganizations = user.getOrganizations()
					.stream()
					.filter((membership)->!membership.isAdmin())
					.map((membership)->membership.getOrganization())
					.collect(Collectors.toSet());
			
			// return fast in special cases
			if (memberOrganizations.contains(account) && account.getDefaultPrivilege() != DepotPrivilege.NONE) {
				return depots;
			}
			
			Collection<Depot> authorizedDepots = user.getAuthorizedDepots()
					.stream()
					.map((authorization)->authorization.getDepot())
					.collect(Collectors.toSet());
			Collection<Team> joinedTeams = user.getJoinedTeams()
					.stream()
					.map((membership)->membership.getTeam())
					.collect(Collectors.toSet());
			
			Collection<TeamAuthorization> authorizations;
			if (account != null)
				authorizations = account.getAllTeamAuthorizationsInOrganization();
			else
				authorizations = teamAuthorizationManager.all();
			
			for (TeamAuthorization authorization: authorizations) {
				if (joinedTeams.contains(authorization.getTeam()))
					authorizedDepots.add(authorization.getDepot());
			}
			
			return depots.stream()
					.filter((depot->depot.isPublicRead() 
							|| depot.getAccount().equals(user)
							|| authorizedDepots.contains(depot)
							|| adminOrganizations.contains(depot.getAccount())
							|| memberOrganizations.contains(depot.getAccount()) 
								&& depot.getAccount().getDefaultPrivilege() != DepotPrivilege.NONE))
					.collect(Collectors.toSet());
		}
	}

}
