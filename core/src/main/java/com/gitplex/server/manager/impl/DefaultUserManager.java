package com.gitplex.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;

import com.gitplex.launcher.loader.Listen;
import com.gitplex.server.event.lifecycle.SystemStarted;
import com.gitplex.server.manager.CacheManager;
import com.gitplex.server.manager.UserManager;
import com.gitplex.server.model.Project;
import com.gitplex.server.model.User;
import com.gitplex.server.model.support.BranchProtection;
import com.gitplex.server.model.support.TagProtection;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.persistence.dao.AbstractEntityManager;
import com.gitplex.server.persistence.dao.Dao;

@Singleton
public class DefaultUserManager extends AbstractEntityManager<User> implements UserManager {

    private final PasswordService passwordService;
    
    private final CacheManager cacheManager;
    
	@Inject
    public DefaultUserManager(Dao dao, CacheManager cacheManager, PasswordService passwordService) {
        super(dao);
        
        this.passwordService = passwordService;
        this.cacheManager = cacheManager;
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
	}

	@Sessional
    @Override
    public User findByName(String userName) {
		Long id = cacheManager.getUserIdByName(userName);
		if (id != null) 
			return load(id);
		else
			return null;
    }

	@Sessional
    @Override
    public User findByEmail(String email) {
		Long id = cacheManager.getUserIdByEmail(email);
		if (id != null) 
			return load(id);
		else
			return null;
    }
	
    @Sessional
    @Override
    public User find(PersonIdent person) {
    	return findByEmail(person.getEmailAddress());
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

	@Listen
	public void on(SystemStarted event) {
		for (User user: findAll()) {
			// Fix a critical issue that password of self-registered users are not hashed
			if (user.getPassword() != null && !user.getPassword().startsWith("$2a$10") 
					&& !user.getPassword().startsWith("@hash^prefix@")) {
				user.setPassword(passwordService.encryptPassword(user.getPassword()));
				save(user);
			}
		}
	}
	
}