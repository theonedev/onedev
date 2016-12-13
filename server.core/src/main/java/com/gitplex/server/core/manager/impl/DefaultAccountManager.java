package com.gitplex.server.core.manager.impl;

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

import com.gitplex.commons.hibernate.Sessional;
import com.gitplex.commons.hibernate.Transactional;
import com.gitplex.commons.hibernate.dao.AbstractEntityManager;
import com.gitplex.commons.hibernate.dao.Dao;
import com.gitplex.commons.hibernate.dao.EntityCriteria;
import com.gitplex.commons.loader.Listen;
import com.gitplex.server.core.entity.Account;
import com.gitplex.server.core.entity.Depot;
import com.gitplex.server.core.entity.PullRequest;
import com.gitplex.server.core.entity.PullRequestStatusChange;
import com.gitplex.server.core.entity.support.IntegrationPolicy;
import com.gitplex.server.core.event.lifecycle.SystemStarting;
import com.gitplex.server.core.event.pullrequest.PullRequestStatusChangeEvent;
import com.gitplex.server.core.manager.AccountManager;
import com.gitplex.server.core.manager.PullRequestManager;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Singleton
public class DefaultAccountManager extends AbstractEntityManager<Account> implements AccountManager {

    private final PullRequestManager pullRequestManager;
    
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    		
	private final BiMap<String, Long> emailToId = HashBiMap.create();
	
	private final BiMap<String, Long> nameToId = HashBiMap.create();
	
	@Inject
    public DefaultAccountManager(Dao dao, PullRequestManager pullRequestManager) {
        super(dao);
        
        this.pullRequestManager = pullRequestManager;
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
    			depot.getGateKeeper().onAccountRename(oldName, account.getName());
    		}
    	}
    	
    	doAfterCommit(new Runnable() {

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
    	Query query = getSession().createQuery("update PullRequest set lastEvent.user=null where lastEvent.user=:lastEventUser");
    	query.setParameter("lastEventUser", account);
    	query.executeUpdate();

    	query = getSession().createQuery("from PullRequest where assignee=:assignee");
    	query.setParameter("assignee", account);

    	for (Object each: query.list()) {
    		PullRequest request = (PullRequest) each;
    		request.setAssignee(request.getTargetDepot().getAccount());
    		pullRequestManager.save(request);
    	}
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.closedBy=null where closeInfo.closedBy=:closedBy");
    	query.setParameter("closedBy", account);
    	query.executeUpdate();
    	
    	query = getSession().createQuery("from PullRequest where submitter=:submitter");
    	query.setParameter("submitter", account);

    	for (Object each: query.list()) {
    		PullRequest request = (PullRequest) each;
    		pullRequestManager.delete(request);
    	}
    	
    	query = getSession().createQuery("update CodeComment set lastEvent.user=null where lastEvent.user=:user");
    	query.setParameter("user", account);
    	query.executeUpdate();
    	
		dao.remove(account);
		
		for (Depot depot: dao.findAll(Depot.class)) {
			for (Iterator<IntegrationPolicy> it = depot.getIntegrationPolicies().iterator(); it.hasNext();) {
				if (it.next().onAccountDelete(account.getName()))
					it.remove();
			}
			depot.getGateKeeper().onAccountDelete(account.getName());
		}
		
		doAfterCommit(new Runnable() {

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
    public Account findByEmail(String email) {
    	idLock.readLock().lock();
    	try {
    		Long id = emailToId.get(email);
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
	@Listen
	public void on(SystemStarting event) {
        for (Account user: findAll()) {
        	if (user.getEmail() != null)
        		emailToId.inverse().put(user.getId(), user.getEmail());
        	nameToId.inverse().put(user.getId(), user.getName());
        }
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

	@Transactional
	@Listen
	public void on(PullRequestStatusChangeEvent event) {
		if (event.getStatusChange().getType() == PullRequestStatusChange.Type.APPROVED
				|| event.getStatusChange().getType() == PullRequestStatusChange.Type.DISAPPROVED) {
			Account user = event.getStatusChange().getUser();
			user.setReviewEffort(user.getReviewEffort()+1);
			save(user);
		}
	}

}