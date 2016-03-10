package com.pmease.gitplex.core.manager.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityDao;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.util.StringUtils;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.DepotManager;

@Singleton
public class DefaultAccountManager extends AbstractEntityDao<Account> implements AccountManager, LifecycleListener {

    private final DepotManager repositoryManager;
    
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    		
	private final Map<String, Set<Long>> emailToIds = new HashMap<>();
	
	private final BiMap<String, Long> nameToId = HashBiMap.create();
	
	@Inject
    public DefaultAccountManager(Dao dao, DepotManager repositoryManager) {
        super(dao);
        
        this.repositoryManager = repositoryManager;
    }

    @Transactional
    @Override
	public void save(Account account, String oldName) {
    	boolean isNew;
    	if (account.isRoot()) {
    		isNew = get(Account.ROOT_ID) == null;
    		getSession().replicate(account, ReplicationMode.OVERWRITE);
    	} else {
    		isNew = account.isNew();
    		persist(account);
    	}

    	if (oldName != null && !oldName.equals(account.getName())) {
    		for (Depot depot: dao.allOf(Depot.class)) {
    			for (IntegrationPolicy integrationPolicy: depot.getIntegrationPolicies()) {
    				integrationPolicy.onAccountRename(oldName, account.getName());
    			}
    			for (GateKeeper gateKeeper: depot.getGateKeepers()) {
    				gateKeeper.onAccountRename(oldName, account.getName());
    			}
    		}
    	}
    	
    	afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					Set<Long> ids = emailToIds.get(account.getEmail());
					if (ids == null) {
						ids = new HashSet<>();
						emailToIds.put(account.getEmail(), ids);
					}
					ids.add(account.getId());
					if (isNew)
						nameToId.inverse().put(account.getId(), account.getName());
				} finally {
					idLock.writeLock().unlock();
				}
			}
    		
    	});
    }
    
    @Sessional
    @Override
    public Account getRoot() {
    	return load(Account.ROOT_ID);
    }

    @Transactional
    @Override
	public void delete(Account account) {
    	Query query = getSession().createQuery("update PullRequest set submitter=null where submitter=:submitter");
    	query.setParameter("submitter", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set assignee.id=:rootId where assignee=:assignee");
    	query.setParameter("rootId", Account.ROOT_ID);
    	query.setParameter("assignee", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.closedBy=null where closeInfo.closedBy=:closedBy");
    	query.setParameter("closedBy", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestActivity set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestActivity set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Comment set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CommentReply set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();

    	for (Depot depot: account.getDepots())
    		repositoryManager.delete(depot);
    	
		remove(account);
		
		for (Depot depot: dao.allOf(Depot.class)) {
			for (Iterator<IntegrationPolicy> it = depot.getIntegrationPolicies().iterator(); it.hasNext();) {
				if (it.next().onAccountDelete(account.getName()))
					it.remove();
			}
			for (Iterator<GateKeeper> it = depot.getGateKeepers().iterator(); it.hasNext();) {
				if (it.next().onAccountDelete(account.getName()))
					it.remove();
			}
		}
		
		afterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					for (Iterator<Map.Entry<String, Set<Long>>> it = emailToIds.entrySet().iterator(); it.hasNext();) {
						Map.Entry<String, Set<Long>> entry = it.next();
						entry.getValue().remove(account.getId());
						if (entry.getValue().isEmpty())
							it.remove();
					}
					nameToId.inverse().remove(account.getId());
				} finally {
					idLock.writeLock().unlock();
				}
			}
			
		});
	}

	@Sessional
    @Override
    public Account findByName(String userName) {
    	idLock.readLock().lock();
    	try {
    		Long id = nameToId.get(userName);
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
    public Account findByPerson(PersonIdent person) {
    	idLock.readLock().lock();
    	try {
    		Set<Long> ids = emailToIds.get(person.getEmailAddress());
    		if (ids != null) {
    			if (ids.size() > 1) {
    				String personName = person.getName().toLowerCase();
    				int minDistance = Integer.MAX_VALUE;
    				Account minDistanceUser = null;
	    			for (Long id: ids) {
	    				Account user = load(id);
	    				int distance;
	    				if (user.getFullName() != null) {
	    					int distance1 = StringUtils.calcLevenshteinDistance(personName, user.getFullName().toLowerCase());
	    					if (distance1 == 0)
	    						return user;
	    					int distance2 = StringUtils.calcLevenshteinDistance(personName, user.getName().toLowerCase());
	    					if (distance2 == 0)
	    						return user;
	    					distance = distance1<distance2?distance1:distance2;
	    				}  else {
	    					distance = StringUtils.calcLevenshteinDistance(personName, user.getName().toLowerCase());
	    					if (distance == 0)
	    						return user;
	    				}
	    				if (distance<minDistance) {
	    					distance = minDistance;
	    					minDistanceUser = user;
	    				}
	    			}
	    			return Preconditions.checkNotNull(minDistanceUser);
    			} else {
    				return load(ids.iterator().next());
    			}
    		} else {
    			return null;
    		}
    	} finally {
    		idLock.readLock().unlock();
    	}
    }
    
    @Override
	public Account getCurrent() {
		Long userId = Account.getCurrentId();
		if (userId != 0L) {
			Account user = get(userId);
			if (user != null)
				return user;
		}
		return null;
	}

	@Override
	public Account getPrevious() {
		Long userId = Account.getPreviousId();
		if (userId != 0L) {
			Account user = get(userId);
			if (user != null)
				return user;
		}
		return null;
	}

	@Sessional
	@Override
	public void systemStarting() {
        for (Account user: all()) {
        	Set<Long> ids = emailToIds.get(user.getEmail());
        	if (ids == null) {
        		ids = new HashSet<>();
        		emailToIds.put(user.getEmail(), ids);
        	}
        	ids.add(user.getId());
        	
        	nameToId.inverse().put(user.getId(), user.getName());
        }
	}

	@Override
	public void systemStarted() {
	}

	@Override
	public void systemStopping() {
	}

	@Override
	public void systemStopped() {
	}

}