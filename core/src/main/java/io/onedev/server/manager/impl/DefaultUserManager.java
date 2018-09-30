package io.onedev.server.manager.impl;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.shiro.authc.credential.PasswordService;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.ReplicationMode;
import org.hibernate.query.Query;

import io.onedev.launcher.loader.Listen;
import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.utils.StringUtils;

@Singleton
public class DefaultUserManager extends AbstractEntityManager<User> implements UserManager {

    private final PasswordService passwordService;
    
    private final ProjectManager projectManager;
    
    private final IssueFieldUnaryManager issueFieldUnaryManager;
    
    private final CacheManager cacheManager;
    
    private final ListenerRegistry listenerRegistry;
    
	@Inject
    public DefaultUserManager(Dao dao, ProjectManager projectManager, 
    		IssueFieldUnaryManager issueFieldUnaryManager, CacheManager cacheManager, 
    		PasswordService passwordService, ListenerRegistry listenerRegistry) {
        super(dao);
        
        this.passwordService = passwordService;
        this.projectManager = projectManager;
        this.issueFieldUnaryManager = issueFieldUnaryManager;
        this.cacheManager = cacheManager;
        this.listenerRegistry = listenerRegistry;
    }

    @Transactional
    @Override
	public void save(User user, String oldName) {
    	if (user.isRoot()) {
    		getSession().replicate(user, ReplicationMode.OVERWRITE);
    		listenerRegistry.post(new EntityPersisted(user, false));
    	} else {
    		dao.persist(user);
    	}

    	if (oldName != null && !oldName.equals(user.getName())) {
    		for (Project project: projectManager.query()) {
    			for (BranchProtection protection: project.getBranchProtections())
    				protection.onRenameUser(project, oldName, user.getName());
    			for (TagProtection protection: project.getTagProtections())
    				protection.onRenameUser(oldName, user.getName());
    			project.getIssueWorkflow().onRenameUser(oldName, user.getName());
    		}
    		
    		issueFieldUnaryManager.onRenameUser(oldName, user.getName());
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
    	Query<?> query = getSession().createQuery("update PullRequest set submitter=null, submitterName=:submitterName "
    			+ "where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("submitterName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.user=null, "
    			+ "closeInfo.userName=:userName where closeInfo.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestAction set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentReply set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Issue set submitter=null, submitterName=:submitterName "
    			+ "where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("submitterName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update IssueComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update IssueAction set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
		dao.remove(user);

		for (Project project: projectManager.query()) {
			for (Iterator<BranchProtection> it = project.getBranchProtections().iterator(); it.hasNext();) { 
				if (it.next().onDeleteUser(project, user.getName()))
					it.remove();
			}
			for (Iterator<TagProtection> it = project.getTagProtections().iterator(); it.hasNext();) { 
				if (it.next().onDeleteUser(user.getName()))
					it.remove();
			}
			project.getIssueWorkflow().onDeleteUser(user.getName());
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
		for (User user: query()) {
			// Fix a critical issue that password of self-registered users are not hashed
			if (StringUtils.isNotBlank(user.getPassword()) && !user.getPassword().startsWith("$2a$10") 
					&& !user.getPassword().startsWith("@hash^prefix@")) {
				user.setPassword(passwordService.encryptPassword(user.getPassword()));
				save(user);
			}
		}
	}
	
}