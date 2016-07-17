package com.pmease.gitplex.core.manager.impl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.Restrictions;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.hibernate.dao.AbstractEntityManager;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.component.IntegrationPolicy;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.listener.LifecycleListener;
import com.pmease.gitplex.core.manager.AccountManager;
import com.pmease.gitplex.core.manager.DepotManager;

@Singleton
public class DefaultAccountManager extends AbstractEntityManager<Account> implements AccountManager, LifecycleListener {

    private final DepotManager repositoryManager;
    
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    		
	private final BiMap<String, Long> emailToId = HashBiMap.create();
	
	private final BiMap<String, Long> nameToId = HashBiMap.create();
	
	@Inject
    public DefaultAccountManager(Dao dao, DepotManager repositoryManager) {
        super(dao);
        
        this.repositoryManager = repositoryManager;
    }

    @Transactional
    @Override
	public void save(Account account, String oldName) {
    	if (account.isAdministrator()) {
    		getSession().replicate(account, ReplicationMode.OVERWRITE);
    	} else {
    		dao.persist(account);
    	}

    	if (oldName != null && !oldName.equals(account.getName())) {
    		for (Depot depot: dao.findAll(Depot.class)) {
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
					nameToId.inverse().put(account.getId(), account.getName());
					if (account.getEmail() != null)
						emailToId.inverse().put(account.getId(), account.getEmail());
				} finally {
					idLock.writeLock().unlock();
				}
			}
    		
    	});
    }
    
    @Sessional
    @Override
    public Account getRoot() {
    	return load(Account.ADMINISTRATOR_ID);
    }

    @Transactional
    @Override
	public void delete(Account account) {
    	Query query = getSession().createQuery("update PullRequest set submitter=null where submitter=:submitter");
    	query.setParameter("submitter", account);
    	query.executeUpdate();

    	query = getSession().createQuery("update PullRequest set lastEventUser=null where lastEventUser=:lastEventUser");
    	query.setParameter("lastEventUser", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set assignee=null where assignee=:assignee");
    	query.setParameter("assignee", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.closedBy=null where closeInfo.closedBy=:closedBy");
    	query.setParameter("closedBy", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestActivity set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestComment set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set user=null where user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();

    	query = getSession().createQuery("update CodeComment set lastUpdateUser=null where lastUpdateUser=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
    	for (Depot depot: account.getDepots())
    		repositoryManager.delete(depot);
    	
		dao.remove(account);
		
		for (Depot depot: dao.findAll(Depot.class)) {
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
					emailToId.inverse().remove(account.getId());
					nameToId.inverse().remove(account.getId());
				} finally {
					idLock.writeLock().unlock();
				}
			}
			
		});
	}

	@Sessional
    @Override
    public Account find(String userName) {
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
    public Account find(PersonIdent person) {
    	idLock.readLock().lock();
    	try {
    		Long id = emailToId.get(person.getEmailAddress());
    		if (id != null)
    			return load(id);
    		else
    			return null;
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

	@Sessional
	@Override
	public void systemStarting() {
        for (Account user: findAll()) {
        	if (user.getEmail() != null)
        		emailToId.inverse().put(user.getId(), user.getEmail());
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

	@Sessional
	@Override
	public List<Account> findAllUsers() {
		EntityCriteria<Account> criteria = EntityCriteria.of(Account.class);
		criteria.add(Restrictions.eq("organization", false));
		return findRange(criteria, 0, 0);
	}

	@Sessional
	@Override
	public List<Account> findAllOrganizations() {
		EntityCriteria<Account> criteria = EntityCriteria.of(Account.class);
		criteria.add(Restrictions.eq("organization", true));
		return findRange(criteria, 0, 0);
	}

}