package com.gitplex.server.manager.impl;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.Restrictions;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStarting;
import com.gitplex.server.manager.AccountManager;
import com.gitplex.server.model.Account;
import com.gitplex.server.model.Depot;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.gitplex.server.persistence.dao.EntityCriteria;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Singleton
public class DefaultAccountManager extends AbstractEntityManager<Account> implements AccountManager {

    private final PasswordService passwordService;
    
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    		
	private final BiMap<String, Long> emailToId = HashBiMap.create();
	
	private final BiMap<String, Long> nameToId = HashBiMap.create();
	
	@Inject
    public DefaultAccountManager(Dao dao, PasswordService passwordService) {
        super(dao);
        
        this.passwordService = passwordService;
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
    			for (BranchProtection protection: depot.getBranchProtections())
    				protection.onAccountRename(depot, oldName, account.getName());
    			for (TagProtection protection: depot.getTagProtections())
    				protection.onAccountRename(oldName, account.getName());
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
    
    @Override
    public void save(Account account) {
    	save(account, null);
    }
    
    @Sessional
    @Override
    public Account getRoot() {
    	return load(Account.ADMINISTRATOR_ID);
    }

    @Transactional
    @Override
	public void delete(Account account) {
    	Query query = getSession().createQuery("update PullRequest set submitter=null, submitterName=:submitterName "
    			+ "where submitter=:submitter");
    	query.setParameter("submitter", account);
    	query.setParameter("submitterName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.closedBy=null, "
    			+ "closeInfo.closedByName=:closedByName where closeInfo.closedBy=:closedBy");
    	query.setParameter("closedBy", account);
    	query.setParameter("closedByName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set lastEvent.user=null, "
    			+ "lastEvent.userName=:lastEventUserName where lastEvent.user=:lastEventUser");
    	query.setParameter("lastEventUser", account);
    	query.setParameter("lastEventUserName", account.getDisplayName());
    	query.executeUpdate();

    	query = getSession().createQuery("update PullRequestStatusChange set user=null, userName=:userName where user=:user");
    	query.setParameter("user", account);
    	query.setParameter("userName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", account);
    	query.setParameter("userName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestReference set user=null, userName=:userName where user=:user");
    	query.setParameter("user", account);
    	query.setParameter("userName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", account);
    	query.setParameter("userName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set lastEvent.user=null, "
    			+ "lastEvent.userName=:lastEventUserName where lastEvent.user=:lastEventUser");
    	query.setParameter("lastEventUser", account);
    	query.setParameter("lastEventUserName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentReply set user=null, userName=:userName where user=:user");
    	query.setParameter("user", account);
    	query.setParameter("userName", account.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentStatusChange set user=null, userName=:userName where user=:user");
    	query.setParameter("user", account);
    	query.setParameter("userName", account.getDisplayName());
    	query.executeUpdate();
    	
		dao.remove(account);
		
		for (Depot depot: dao.findAll(Depot.class)) {
			for (BranchProtection protection: depot.getBranchProtections())
				protection.onAccountDelete(depot, account.getName());
			for (TagProtection protection: depot.getTagProtections())
				protection.onAccountDelete(account.getName());
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

	@Listen
	public void on(SystemStarted event) {
		// Fix a critical issue that password of self-registered users are not hashed
		for (Account account: findAll()) {
			if (account.getPassword() != null && !account.getPassword().startsWith("$2a$10") 
					&& !account.getPassword().startsWith("@hash^prefix@")) {
				account.setPassword(passwordService.encryptPassword(account.getPassword()));
				save(account);
			}
		}
	}

}