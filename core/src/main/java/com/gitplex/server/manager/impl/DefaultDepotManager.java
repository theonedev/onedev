package com.gitplex.server.manager.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.server.GitPlex;
import com.gitplex.server.event.RefUpdated;
import com.gitplex.server.event.depot.DepotDeleted;
import com.gitplex.server.event.depot.DepotRenamed;
import com.gitplex.server.event.depot.DepotTransferred;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStarting;
import com.gitplex.server.event.lifecycle.SystemStopping;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.git.command.CloneCommand;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.manager.CommitInfoManager;
import com.gitplex.server.manager.DepotManager;
import com.gitplex.server.manager.StorageManager;
import com.gitplex.server.manager.TeamAuthorizationManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.Team;
import com.gitplex.server.model.TeamAuthorization;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.security.privilege.DepotPrivilege;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.Pair;
import com.gitplex.server.util.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Singleton
public class DefaultDepotManager extends AbstractEntityManager<Depot> implements DepotManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultDepotManager.class);
	
	private static final int INFO_VERSION = 3;
	
	private final ListenerRegistry listenerRegistry;
	
    private final AccountManager userManager;
    
    private final CommitInfoManager commitInfoManager;
    
    private final TeamAuthorizationManager teamAuthorizationManager;
    
    private final StorageManager storageManager;
    
    private final String gitReceiveHook;
    
	private final BiMap<Pair<Long, String>, Long> nameToId = HashBiMap.create();
	
	private final ReadWriteLock idLock = new ReentrantReadWriteLock();
	
	private final Map<Long, Repository> repositoryCache = new ConcurrentHashMap<>();
	
    @Inject
    public DefaultDepotManager(Dao dao, AccountManager userManager, TeamAuthorizationManager teamAuthorizationManager, 
    		CommitInfoManager commitInfoManager, ListenerRegistry listenerRegistry, StorageManager storageManager) {
    	super(dao);
    	
        this.userManager = userManager;
        this.teamAuthorizationManager = teamAuthorizationManager;
        this.commitInfoManager = commitInfoManager;
        this.listenerRegistry = listenerRegistry;
        this.storageManager = storageManager;
        
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
						repository = new FileRepository(depot.getGitDir());
						repository.getConfig().setEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, 
								ConfigConstants.CONFIG_KEY_ALGORITHM, SupportedAlgorithm.HISTOGRAM);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
    				repositoryCache.put(depot.getId(), repository);
    			}
    		}
    	}
    	return repository;
    }
    
    @Override
    public void save(Depot depot) {
    	save(depot, null, null);
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
    		
       		for (BranchProtection protection: depot.getBranchProtections()) {
				protection.onDepotTransferred(depot);
			}
       		for (TagProtection protection: depot.getTagProtections()) {
				protection.onDepotTransferred(depot);
			}
    	}
    	if (oldName != null && !depot.getName().equals(oldName)) {
    		listenerRegistry.post(new DepotRenamed(depot, oldName));
    	}
    	
        doAfterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(depot.getId(), new Pair<>(depot.getAccount().getId(), depot.getName()));
				} finally {
					idLock.writeLock().unlock();
				}
				if (isNew)
		    		checkDirectory(depot);
			}
        	
        });
    }
    
    @Transactional
    @Override
    public void delete(Depot depot) {
    	Query query = getSession().createQuery("update Depot set forkedFrom=null where forkedFrom=:forkedFrom");
    	query.setParameter("forkedFrom", depot);
    	query.executeUpdate();

    	dao.remove(depot);
    	
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
    	Account user = userManager.findByName(accountName);
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
    	Account user = userManager.findByName(userName);
    	if (user != null)
    		return find(user, StringUtils.substringAfter(depotFQN, Depot.FQN_SEPARATOR));
    	else
    		return null;
    }

    @Transactional
	@Override
	public void fork(Depot from, Depot to) {
    	save(to);
        FileUtils.cleanDir(to.getGitDir());
        new CloneCommand(to.getGitDir()).mirror(true).from(from.getGitDir().getAbsolutePath()).call();
        
        doAfterCommit(new Runnable() {

			@Override
			public void run() {
		        commitInfoManager.cloneInfo(from, to);
			}
        	
        });
	}

	private boolean isGitHookValid(Depot depot, String hookName) {
        File hookFile = new File(depot.getGitDir(), "hooks/" + hookName);
        if (!hookFile.exists()) 
        	return false;
        
        try {
			String content = FileUtils.readFileToString(hookFile);
			if (!content.contains("GITPLEX_USER_ID"))
				return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
        if (!hookFile.canExecute())
        	return false;
        
        return true;
	}
	
	private void checkDirectory(Depot depot) {
		File gitDir = depot.getGitDir();
		if (depot.getGitDir().exists() && !GitUtils.isValid(gitDir)) {
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
        
        if (!isGitHookValid(depot, "pre-receive") || !isGitHookValid(depot, "post-receive")) {
            File hooksDir = new File(gitDir, "hooks");

            File gitPreReceiveHookFile = new File(hooksDir, "pre-receive");
            FileUtils.writeFile(gitPreReceiveHookFile, String.format(gitReceiveHook, "git-prereceive-callback"));
            gitPreReceiveHookFile.setExecutable(true);
            
            File gitPostReceiveHookFile = new File(hooksDir, "post-receive");
            FileUtils.writeFile(gitPostReceiveHookFile, String.format(gitReceiveHook, "git-postreceive-callback"));
            gitPostReceiveHookFile.setExecutable(true);
        }
        
		File infoVersionFile = new File(storageManager.getInfoDir(depot), "version.txt");
		int infoVersion;
		if (infoVersionFile.exists()) {
			try {
				infoVersion = Integer.parseInt(FileUtils.readFileToString(infoVersionFile).trim());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			infoVersion = 0;
		}
		if (infoVersion != INFO_VERSION) {
			FileUtils.cleanDir(infoVersionFile.getParentFile());
			FileUtils.writeFile(infoVersionFile, String.valueOf(INFO_VERSION));
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
			logger.info("Checking repository {}...", depot.getFQN());
			checkDirectory(depot);
	        try {
				commitInfoManager.requestToCollect(depot).get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
			logger.info("Repository checking finished for {}", depot.getFQN());
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
		if (event.getNewObjectId().equals(ObjectId.zeroId())) {
			Depot depot = event.getDepot();
			String branch = GitUtils.ref2branch(event.getRefName());
			if (branch != null) {
				for (Iterator<BranchProtection> it = depot.getBranchProtections().iterator(); it.hasNext();) {
					if (it.next().onBranchDelete(branch))	
						it.remove();
				}
			}
			String tag = GitUtils.ref2tag(event.getRefName());
			if (tag != null) {
				for (Iterator<TagProtection> it = depot.getTagProtections().iterator(); it.hasNext();) {
					if (it.next().onTagDelete(tag))	
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
