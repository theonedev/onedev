package com.pmease.gitplex.core.manager.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.ReplicationMode;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.events.SystemStarting;
import com.pmease.gitplex.core.manager.PullRequestManager;
import com.pmease.gitplex.core.manager.RepositoryManager;
import com.pmease.gitplex.core.manager.UserManager;
import com.pmease.gitplex.core.model.Membership;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.Team;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.permission.operation.GeneralOperation;

@Singleton
public class DefaultUserManager implements UserManager {

    private final Dao dao;

    private final RepositoryManager repositoryManager;
    
    private final PullRequestManager pullRequestManager;
    
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    		
	private final BiMap<String, Long> emailToId = HashBiMap.create();
	
	private final BiMap<String, Long> nameToId = HashBiMap.create();
	
	@Inject
    public DefaultUserManager(EventBus eventBus, Dao dao, 
    		RepositoryManager repositoryManager, PullRequestManager pullRequestManager) {
		eventBus.register(this);
        this.dao = dao;
        this.repositoryManager = repositoryManager;
        this.pullRequestManager = pullRequestManager;
    }

    @Transactional
    @Override
	public void save(final User user) {
    	boolean isNew;
    	if (user.isRoot()) {
    		isNew = dao.get(User.class, User.ROOT_ID) == null;
    		dao.getSession().replicate(user, ReplicationMode.OVERWRITE);
    	} else {
    		isNew = user.isNew();
    		dao.persist(user);
    	}
    	
    	if (isNew) {
        	Team team = new Team();
        	team.setOwner(user);
        	team.setAuthorizedOperation(GeneralOperation.NO_ACCESS);
        	team.setName(Team.ANONYMOUS);
        	dao.persist(team);
        	
        	team = new Team();
        	team.setOwner(user);
        	team.setName(Team.LOGGEDIN);
        	team.setAuthorizedOperation(GeneralOperation.NO_ACCESS);
        	dao.persist(team);
        	
        	team = new Team();
        	team.setOwner(user);
        	team.setName(Team.OWNERS);
        	team.setAuthorizedOperation(GeneralOperation.ADMIN);
        	dao.persist(team);
        	
        	Membership membership = new Membership();
        	membership.setTeam(team);
        	membership.setUser(user);
        	dao.persist(membership);
    	}
    	
    	dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					emailToId.inverse().put(user.getId(), user.getEmail());
					nameToId.inverse().put(user.getId(), user.getName());
				} finally {
					idLock.writeLock().unlock();
				}
			}
    		
    	});
    }

    @Sessional
    @Override
    public User getRoot() {
    	return dao.load(User.class, User.ROOT_ID);
    }

    @Transactional
    @Override
	public void delete(final User user) {
    	Query query = dao.getSession().createQuery("update Branch set lastUpdater=null where lastUpdater=:lastUpdater");
    	query.setParameter("lastUpdater", user);
    	query.executeUpdate();
    	
    	query = dao.getSession().createQuery("update PullRequestUpdate set user=null where user=:user");
    	query.setParameter("user", user);
    	query.executeUpdate();

    	for (Repository repository: user.getRepositories())
    		repositoryManager.delete(repository);
    	
    	for (PullRequest request: user.getSubmittedRequests())
    		pullRequestManager.delete(request);
    	
		dao.remove(user);
		
		dao.afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					emailToId.inverse().remove(user.getId());
					nameToId.inverse().remove(user.getId());
				} finally {
					idLock.writeLock().unlock();
				}
			}
			
		});
	}

	@Sessional
    @Override
    public User findByName(String userName) {
    	idLock.readLock().lock();
    	try {
    		Long id = nameToId.get(userName);
    		if (id != null)
    			return dao.load(User.class, id);
    		else
    			return null;
    	} finally {
    		idLock.readLock().unlock();
    	}
    }

    @Sessional
    @Override
    public User findByEmail(String email) {
    	idLock.readLock().lock();
    	try {
    		Long id = emailToId.get(email);
    		if (id != null)
    			return dao.load(User.class, id);
    		else
    			return null;
    	} finally {
    		idLock.readLock().unlock();
    	}
    }
    
    @Override
	public User getCurrent() {
		Long userId = User.getCurrentId();
		if (userId != 0L) {
			User user = dao.get(User.class, userId);
			if (user != null)
				return user;
		}
		return null;
	}

	@Override
	public void trim(Collection<Long> userIds) {
		for (Iterator<Long> it = userIds.iterator(); it.hasNext();) {
			if (dao.get(User.class, it.next()) == null)
				it.remove();
		}
	}

	@Override
	@Sessional
	public List<User> getManagableAccounts(User user) {
		Preconditions.checkNotNull(user);
		Collection<Membership> memberships = user.getMemberships();
		List<User> result = Lists.newArrayList();
		for (Membership each : memberships) {
			if (each.getTeam().isOwners()) {
				result.add(each.getTeam().getOwner());
			}
		}
		
		return result;
	}

	@Subscribe
	@Sessional
	public void systemStarting(SystemStarting event) {
        for (User user: dao.allOf(User.class)) {
        	emailToId.inverse().put(user.getId(), user.getEmail());
        	nameToId.inverse().put(user.getId(), user.getName());
        }
	}

}