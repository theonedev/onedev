package com.pmease.gitplex.core.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.git.command.CloneCommand;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.Listen;
import com.pmease.commons.loader.ListenerRegistry;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.util.Pair;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.Team;
import com.pmease.gitplex.core.entity.TeamAuthorization;
import com.pmease.gitplex.core.entity.support.IntegrationPolicy;
import com.pmease.gitplex.core.event.RefUpdated;
import com.pmease.gitplex.core.event.depot.DepotDeleted;
import com.pmease.gitplex.core.event.depot.DepotRenamed;
import com.pmease.gitplex.core.event.depot.DepotTransferred;
import com.pmease.gitplex.core.event.lifecycle.SystemStarted;
import com.pmease.gitplex.core.event.lifecycle.SystemStarting;
import com.pmease.gitplex.core.event.lifecycle.SystemStopping;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.CodeCommentInfoManager;
import com.pmease.gitplex.core.manager.CommitInfoManager;
import com.pmease.gitplex.core.manager.DepotManager;
import com.pmease.gitplex.core.manager.PullRequestInfoManager;
import com.pmease.gitplex.core.manager.TeamAuthorizationManager;
import com.pmease.gitplex.core.security.privilege.DepotPrivilege;

@Singleton
public class DefaultDepotManager extends AbstractEntityManager<Depot> implements DepotManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDepotManager.class);
	
	private final ListenerRegistry listenerRegistry;
	
    private final AccountManager userManager;
    
    private final CommitInfoManager commitInfoManager;
    
    private final PullRequestInfoManager pullRequestInfoManager;
    
    private final CodeCommentInfoManager codeCommentInfoManager;
   
    private final TeamAuthorizationManager teamAuthorizationManager;
    
    private final String gitReceiveHook;
    
	private final BiMap<Pair<Long, String>, Long> nameToId = HashBiMap.create();
	
	private final ReadWriteLock idLock = new ReentrantReadWriteLock();
	
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultDepotManager(Dao dao, AccountManager userManager, CodeCommentInfoManager codeCommentInfoManager,
    		TeamAuthorizationManager teamAuthorizationManager, PullRequestInfoManager pullRequestInfoManager,
    		CommitInfoManager commitInfoManager, ListenerRegistry listenerRegistry) {
    	super(dao);
    	
        this.userManager = userManager;
        this.pullRequestInfoManager = pullRequestInfoManager;
        this.teamAuthorizationManager = teamAuthorizationManager;
        this.commitInfoManager = commitInfoManager;
        this.codeCommentInfoManager = codeCommentInfoManager;
        this.listenerRegistry = listenerRegistry;
        
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("git-receive-hook")) {
        	Preconditions.checkNotNull(is);
            gitReceiveHook = StringUtils.join(IOUtils.readLines(is), "\n");
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
						repository = new FileRepository(depot.getDirectory());
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
    	
    	dao.persist(depot);
    	
    	if (oldAccountId != null && !depot.getAccount().getId().equals(oldAccountId)) {
    		Account oldAccount = userManager.load(oldAccountId);
    		listenerRegistry.post(new DepotTransferred(depot, oldAccount));
    		
    		for (Depot each: findAll()) {
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
    		listenerRegistry.post(new DepotRenamed(depot, oldName));
    		
    		for (Depot each: findAll()) {
    			for (IntegrationPolicy integrationPolicy: each.getIntegrationPolicies()) {
    				integrationPolicy.onDepotRename(depot.getAccount(), oldName, depot.getName());
    			}
    			for (GateKeeper gateKeeper: each.getGateKeepers()) {
    				gateKeeper.onDepotRename(depot, oldName);
    			}
    		}
    	}
    	
        doAfterCommit(newAfterCommitRunnable(depot, isNew));
    }
    
    private Runnable newAfterCommitRunnable(Depot depot, boolean isNew) {
    	return new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(depot.getId(), new Pair<>(depot.getAccount().getId(), depot.getName()));
				} finally {
					idLock.writeLock().unlock();
				}
				if (isNew) {
		    		checkSanity(depot);
				}
			}
        	
        };    	
    }

    @Transactional
    @Override
    public void delete(Depot depot) {
    	Query query = getSession().createQuery("update Depot set forkedFrom=null where forkedFrom=:forkedFrom");
    	query.setParameter("forkedFrom", depot);
    	query.executeUpdate();

    	dao.remove(depot);
    	
		for (Depot each: findAll()) {
			for (Iterator<IntegrationPolicy> it = each.getIntegrationPolicies().iterator(); it.hasNext();) {
				if (it.next().onDepotDelete(depot))
					it.remove();
			}
			for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onDepotDelete(depot))
					it.remove();
			}
		}

		doAfterCommit(new Runnable() {

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
			}
			
		});

		listenerRegistry.post(new DepotDeleted(depot));
    }
    
    @Sessional
    @Override
    public Depot find(String accountName, String depotName) {
    	Account user = userManager.find(accountName);
    	if (user != null)
    		return find(user, depotName);
    	else
    		return null;
    }

    @Sessional
    @Override
    public Depot find(Account account, String depotName) {
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
    public Depot find(String depotFQN) {
    	String userName = StringUtils.substringBefore(depotFQN, Depot.FQN_SEPARATOR);
    	Account user = userManager.find(userName);
    	if (user != null)
    		return find(user, StringUtils.substringAfter(depotFQN, Depot.FQN_SEPARATOR));
    	else
    		return null;
    }

    @Transactional
	@Override
	public void fork(Depot from, Depot to) {
    	boolean isNew = to.isNew();
    	save(to);
        FileUtils.cleanDir(to.getDirectory());
        new CloneCommand(to.getDirectory()).mirror(true).from(from.getDirectory().getAbsolutePath()).call();
        doAfterCommit(newAfterCommitRunnable(to, isNew));
	}

	private void checkSanity(Depot depot) {
		logger.info("Checking sanity of repository '{}'...", depot);
		File gitDir = depot.getDirectory();
		if (depot.getDirectory().exists() && !GitUtils.isValid(gitDir)) {
        	logger.warn("Directory '" + gitDir + "' is not a valid git repository, removing...");
        	FileUtils.deleteDir(gitDir);
        }
        
        if (!gitDir.exists()) {
        	logger.warn("Initializing git repository in '" + gitDir + "'...");
            FileUtils.createDir(gitDir);
            try {
				Git.init().setDirectory(gitDir).setBare(true).call();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
        }
        
        if (!depot.isValid()) {
            File hooksDir = new File(gitDir, "hooks");

            File gitPreReceiveHookFile = new File(hooksDir, "pre-receive");
            FileUtils.writeFile(gitPreReceiveHookFile, String.format(gitReceiveHook, "git-prereceive-callback"));
            gitPreReceiveHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, String.format(gitReceiveHook, "git-postreceive-callback"));
            gitPostReceiveHookFile.setExecutable(true);
        }
	}
	
	@Sessional
	@Listen
	public void on(SystemStarting event) {
        for (Depot depot: findAll()) 
        	nameToId.inverse().put(depot.getId(), new Pair<>(depot.getAccount().getId(), depot.getName()));
	}
	
	@Transactional
	@Listen
	public void on(SystemStarted event) {
		for (Depot depot: findAll()) {
			checkSanity(depot);
	        commitInfoManager.collect(depot);
	        pullRequestInfoManager.collect(depot);
	        codeCommentInfoManager.collect(depot);
		}
	}

	@Listen
	public void on(SystemStopping event) {
		synchronized(repositoryCache) {
			for (Repository repository: repositoryCache.values()) {
				repository.close();
			}
		}
	}

	@Transactional
	@Listen
	public void on(RefUpdated event) {
		if (event.getNewCommit().equals(ObjectId.zeroId())) {
			String branch = GitUtils.ref2branch(event.getRefName());
			for (Depot each: findAll()) {
				if (branch != null) {
					for (Iterator<IntegrationPolicy> it = each.getIntegrationPolicies().iterator(); it.hasNext();) {
						if (it.next().onBranchDelete(each, event.getDepot(), branch))
							it.remove();
					}
				}
				for (Iterator<GateKeeper> it = each.getGateKeepers().iterator(); it.hasNext();) {
					if (it.next().onRefDelete(event.getRefName()))
						it.remove();
				}
			}
		}
	}

	@Sessional
	@Override
	public Collection<Depot> findAllAccessible(Account account, Account user) {
		Collection<Depot> depots;
		if (account == null)
			depots = GitPlex.getInstance(DepotManager.class).findAll();
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
				authorizations = teamAuthorizationManager.findAll();
			
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
