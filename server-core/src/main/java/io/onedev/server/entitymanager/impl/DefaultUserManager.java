package io.onedev.server.entitymanager.impl;

import static io.onedev.server.model.User.PROP_PASSWORD;
import static io.onedev.server.model.User.PROP_SSO_INFO;
import static io.onedev.server.model.support.SsoInfo.PROP_CONNECTOR;
import static io.onedev.server.model.support.SsoInfo.PROP_SUBJECT;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultUserManager extends BaseEntityManager<User> implements UserManager {

    private final ProjectManager projectManager;
    
    private final SettingManager settingManager;
    
    private final IssueFieldManager issueFieldManager;
    
    private final IdManager idManager;
    
    private final TransactionManager transactionManager;
    
    private final Map<String, Long> userIdByEmail = new ConcurrentHashMap<>();
    
    private final Map<Long, UserFacade> cache = new ConcurrentHashMap<>();
    
	@Inject
    public DefaultUserManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
    		IssueFieldManager issueFieldManager, IdManager idManager, TransactionManager transactionManager) {
        super(dao);
        
        this.projectManager = projectManager;
        this.settingManager = settingManager;
        this.issueFieldManager = issueFieldManager;
        this.idManager = idManager;
        this.transactionManager = transactionManager;
    }

	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		String queryString = String.format("select id, %s, %s, %s, %s, %s from User", 
				User.PROP_NAME, User.PROP_FULL_NAME, User.PROP_EMAIL, User.PROP_GIT_EMAIL, User.PROP_ALTERNATE_EMAILS);
		Query<?> query = dao.getSession().createQuery(queryString);
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long userId = (Long) fields[0];
			String name = (String) fields[1];
			String fullName = (String) fields[2];
			String email = (String) fields[3];
			String gitEmail = (String) fields[4];
			List<String> alternateEmails = (List<String>) fields[5];
			
			userIdByEmail.put(email, userId);
			if (gitEmail != null)
				userIdByEmail.put(gitEmail, userId);
			for (String alternateEmail: alternateEmails)
				userIdByEmail.put(alternateEmail, userId);
			cache.put(userId, new UserFacade(userId, name, fullName, email, gitEmail, alternateEmails));
		}
	}
	
	@Transactional
	@Override
	public void replicate(User user) {
		getSession().replicate(user, ReplicationMode.OVERWRITE);
		idManager.useId(User.class, user.getId());
	}
	
    @Transactional
    @Override
	public void save(User user, String oldName) {
    	dao.persist(user);

    	if (oldName != null && !oldName.equals(user.getName())) {
    		for (Project project: projectManager.query()) {
    			for (BranchProtection protection: project.getBranchProtections())
    				protection.onRenameUser(oldName, user.getName());
    			for (TagProtection protection: project.getTagProtections())
    				protection.onRenameUser(oldName, user.getName());
    			project.getIssueSetting().onRenameUser(oldName, user.getName());
    		}
    		
    		settingManager.onRenameUser(oldName, user.getName());
    		
    		issueFieldManager.onRenameUser(oldName, user.getName());
    	}
    	
    	transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				userIdByEmail.put(user.getEmail(), user.getId());
				if (user.getGitEmail() != null)
					userIdByEmail.put(user.getGitEmail(), user.getId());
				for (String alternateEmail: user.getAlternateEmails())
					userIdByEmail.put(alternateEmail, user.getId());
				cache.put(user.getId(), new UserFacade(user));
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

    @Sessional
    @Override
    public User getSystem() {
    	return load(User.SYSTEM_ID);
    }
    
    @Sessional
    @Override
    public User getUnknown() {
    	return load(User.UNKNOWN_ID);
    }
    
    @Transactional
    @Override
	public void delete(User user) {
    	Usage usage = new Usage();
		for (Project project: projectManager.query()) {
			Usage usedInProject = new Usage();
			for (BranchProtection protection: project.getBranchProtections()) 
				usedInProject.add(protection.onDeleteUser(user.getName()));
			for (TagProtection protection: project.getTagProtections()) 
				usedInProject.add(protection.onDeleteUser(user.getName()));
			usedInProject.add(project.getIssueSetting().onDeleteUser(user.getName()));
			usedInProject.prefix("project '" + project.getName() + "': setting");
			usage.add(usedInProject);
		}

		usage.add(settingManager.onDeleteUser(user.getName()));
		
		usage.checkInUse("User '" + user.getName() + "'");
    	
    	Query<?> query = getSession().createQuery("update PullRequest set submitter=:unknown where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Build set submitter=:unknown where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Build set canceller=:unknown where canceller=:canceller");
    	query.setParameter("canceller", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.user=:unknown where closeInfo.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set lastUpdate.user=:unknown where lastUpdate.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestChange set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestComment set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeComment set lastUpdate.user=:unknown where lastUpdate.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentReply set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Issue set submitter=:unknown where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Issue set lastUpdate.user=:unknown where lastUpdate.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update IssueComment set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update IssueChange set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
		dao.remove(user);
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				cache.remove(user.getId());
				userIdByEmail.remove(user.getEmail());
				if (user.getGitEmail() != null)
					userIdByEmail.remove(user.getGitEmail());
				for (String email: user.getAlternateEmails())
					userIdByEmail.remove(email);
			}
			
		});
    }

	@Sessional
    @Override
    public User findByName(String userName) {
		EntityCriteria<User> criteria = newCriteria();
		criteria.add(Restrictions.ilike(User.PROP_NAME, userName));
		criteria.setCacheable(true);
		return find(criteria);
    }

	@Sessional
    @Override
    public User findByFullName(String fullName) {
		EntityCriteria<User> criteria = newCriteria();
		criteria.add(Restrictions.ilike(User.PROP_FULL_NAME, fullName));
		criteria.setCacheable(true);
		return find(criteria);
    }
	
	@Sessional
    @Override
    public User findByAccessToken(String accessToken) {
		EntityCriteria<User> criteria = newCriteria();
		criteria.add(Restrictions.eq(User.PROP_ACCESS_TOKEN, accessToken));
		criteria.setCacheable(true);
		return find(criteria);
    }
	
	@Sessional
    @Override
    public User findBySsoInfo(SsoInfo ssoInfo) {
		EntityCriteria<User> criteria = newCriteria();
		criteria.add(Restrictions.eq(User.PROP_SSO_INFO + "." + SsoInfo.PROP_CONNECTOR, ssoInfo.getConnector()));
		criteria.add(Restrictions.eq(User.PROP_SSO_INFO + "." + SsoInfo.PROP_SUBJECT, ssoInfo.getSubject()));
		criteria.setCacheable(true);
		return find(criteria);
    }
	
	@Override
	public List<User> query() {
		EntityCriteria<User> criteria = newCriteria();
		criteria.add(Restrictions.gt("id", 0L));
		criteria.setCacheable(true);
		return query(criteria);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Sessional
    @Override
    public UserFacade findFacadeByEmail(String email) {
		Long userId = userIdByEmail.get(email);
		if (userId != null) {
			UserFacade user = cache.get(userId);
			if (user != null && user.isUsingEmail(email))
				return user;
		}
		return null;
    }
	
    @Sessional
    @Override
    public UserFacade findFacade(PersonIdent person) {
    	if (StringUtils.isNotBlank(person.getEmailAddress()))
    		return findFacadeByEmail(person.getEmailAddress());
    	else
    		return null;
    }
    
    @Sessional
    @Override
    public UserFacade getFacade(Long userId) {
    	return cache.get(userId);
    }
    
	@Sessional
    @Override
    public User findByEmail(String email) {
		UserFacade facade = findFacadeByEmail(email);
		return facade!=null? load(facade.getId()): null;
    }
	
    @Sessional
    @Override
    public User find(PersonIdent person) {
    	UserFacade facade = findFacade(person);
		return facade!=null? load(facade.getId()): null;
    }
    
	@Override
	public List<User> queryAndSort(Collection<User> topUsers) {
		List<User> users = query();
		users.sort(Comparator.comparing(User::getDisplayName));
		users.removeAll(topUsers);
		users.addAll(0, topUsers);
		return users;
	}

	@Transactional
	@Override
	public void onRenameSsoConnector(String oldName, String newName) {
		String connectorProp = PROP_SSO_INFO + "." + PROP_CONNECTOR;
    	Query<?> query = getSession().createQuery(String.format("update User set %s=:newName "
    			+ "where %s=:oldName", connectorProp, connectorProp));
    	query.setParameter("oldName", oldName);
    	query.setParameter("newName", newName);
    	query.executeUpdate();
	}

	@Transactional
	@Override
	public void onDeleteSsoConnector(String name) {
		String connectorProp = PROP_SSO_INFO + "." + PROP_CONNECTOR;
		String subjectProp = PROP_SSO_INFO + "." + PROP_SUBJECT;
    	Query<?> query = getSession().createQuery(String.format("update User set %s=null, %s='%s', %s='12345' "
    			+ "where %s=:name", 
    			connectorProp, subjectProp, UUID.randomUUID().toString(), PROP_PASSWORD, connectorProp));
    	query.setParameter("name", name);
    	query.executeUpdate();
	}

}