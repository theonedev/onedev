package com.gitplex.server.manager.impl;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.event.lifecycle.SystemStarting;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

@Singleton
public class DefaultUserManager extends AbstractEntityManager<User> implements UserManager {

    private final PasswordService passwordService;
    
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();
    		
	private final BiMap<String, Long> emailToId = HashBiMap.create();
	
	private final BiMap<String, Long> nameToId = HashBiMap.create();
	
	@Inject
    public DefaultUserManager(Dao dao, PasswordService passwordService) {
        super(dao);
        
        this.passwordService = passwordService;
    }

    @Transactional
    @Override
	public void save(User user, String oldName) {
    	if (user.isRoot()) {
    		getSession().replicate(user, ReplicationMode.OVERWRITE);
    	} else {
    		dao.persist(user);
    	}

    	if (oldName != null && !oldName.equals(user.getName())) {
    		for (Project project: dao.findAll(Project.class)) {
    			for (BranchProtection protection: project.getBranchProtections())
    				protection.onUserRename(project, oldName, user.getName());
    			for (TagProtection protection: project.getTagProtections())
    				protection.onUserRename(oldName, user.getName());
    		}
    	}
    	
    	doAfterCommit(new Runnable() {

			@Override
			public void run() {
				idLock.writeLock().lock();
				try {
					nameToId.inverse().put(user.getId(), user.getName());
					if (user.getEmail() != null)
						emailToId.inverse().put(user.getId(), user.getEmail());
				} finally {
					idLock.writeLock().unlock();
				}
			}
    		
    	});
    }
    
    @Override
    public void save(User user) {
    	save(user, null);
    }
    
    @Sessional
    @Override
    public User getRoot() {
    	return load(User.ROOT_ID);
    }

    @Transactional
    @Override
	public void delete(User user) {
    	Query query = getSession().createQuery("update PullRequest set submitter=null, submitterName=:submitterName "
    			+ "where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("submitterName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.closedBy=null, "
    			+ "closeInfo.closedByName=:closedByName where closeInfo.closedBy=:closedBy");
    	query.setParameter("closedBy", user);
    	query.setParameter("closedByName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set lastEvent.user=null, "
    			+ "lastEvent.userName=:lastEventUserName where lastEvent.user=:lastEventUser");
    	query.setParameter("lastEventUser", user);
    	query.setParameter("lastEventUserName", user.getDisplayName());
    	query.executeUpdate();

    	query = getSession().createQuery("update PullRequestStatusChange set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestReference set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set lastEvent.user=null, "
    			+ "lastEvent.userName=:lastEventUserName where lastEvent.user=:lastEventUser");
    	query.setParameter("lastEventUser", user);
    	query.setParameter("lastEventUserName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentReply set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentStatusChange set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
		dao.remove(user);
		
		for (Project project: dao.findAll(Project.class)) {
			for (BranchProtection protection: project.getBranchProtections())
				protection.onUserDelete(project, user.getName());
			for (TagProtection protection: project.getTagProtections())
				protection.onUserDelete(user.getName());
		}
		
		doAfterCommit(new Runnable() {

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
    			return load(id);
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
    			return load(id);
    		else
    			return null;
    	} finally {
    		idLock.readLock().unlock();
    	}
    }
	
    @Sessional
    @Override
    public User find(PersonIdent person) {
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
	public User getCurrent() {
		Long userId = User.getCurrentId();
		if (userId != 0L) {
			User user = get(userId);
			if (user != null)
				return user;
		}
		return null;
	}

	@Sessional
	@Listen
	public void on(SystemStarting event) {
        for (User user: findAll()) {
        	if (user.getEmail() != null)
        		emailToId.inverse().put(user.getId(), user.getEmail());
        	nameToId.inverse().put(user.getId(), user.getName());
        }
	}

	@Listen
	public void on(SystemStarted event) {
		// Fix a critical issue that password of self-registered users are not hashed
		for (User user: findAll()) {
			if (user.getPassword() != null && !user.getPassword().startsWith("$2a$10") 
					&& !user.getPassword().startsWith("@hash^prefix@")) {
				user.setPassword(passwordService.encryptPassword(user.getPassword()));
				save(user);
			}
		}
	}

}