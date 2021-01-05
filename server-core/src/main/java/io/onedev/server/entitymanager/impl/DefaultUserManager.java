package io.onedev.server.entitymanager.impl;

import static io.onedev.server.model.User.PROP_PASSWORD;
import static io.onedev.server.model.User.PROP_SSO_INFO;
import static io.onedev.server.model.support.SsoInfo.PROP_CONNECTOR;
import static io.onedev.server.model.support.SsoInfo.PROP_SUBJECT;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.PersonIdent;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.BranchProtection;
import io.onedev.server.model.support.SsoInfo;
import io.onedev.server.model.support.TagProtection;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultUserManager extends BaseEntityManager<User> implements UserManager {

    private final ProjectManager projectManager;
    
    private final SettingManager settingManager;
    
    private final IssueFieldManager issueFieldManager;
    
    private final IdManager idManager;
    
	@Inject
    public DefaultUserManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
    		IssueFieldManager issueFieldManager, IdManager idManager) {
        super(dao);
        
        this.projectManager = projectManager;
        this.settingManager = settingManager;
        this.issueFieldManager = issueFieldManager;
        this.idManager = idManager;
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
    		
        	for (JobExecutor jobExecutor: settingManager.getJobExecutors())
        		jobExecutor.onRenameUser(oldName, user.getName());
    		
    		issueFieldManager.onRenameUser(oldName, user.getName());
    		settingManager.getIssueSetting().onRenameUser(oldName, user.getName());
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

    @Sessional
    @Override
    public User getSystem() {
    	return load(User.SYSTEM_ID);
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
		
    	int index = 0;
    	for (JobExecutor jobExecutor: settingManager.getJobExecutors()) {
    		usage.add(jobExecutor.onDeleteUser(user.getName(), index).prefix("administration"));
    		index++;
    	}

		usage.add(settingManager.getIssueSetting().onDeleteUser(user.getName()).prefix("administration"));
		
		usage.checkInUse("User '" + user.getName() + "'");
    	
    	Query<?> query = getSession().createQuery("update PullRequest set submitter=null, submitterName=:submitterName "
    			+ "where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("submitterName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Build set submitter=null, submitterName=:submitterName "
    			+ "where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("submitterName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Build set canceller=null, cancellerName=:cancellerName "
    			+ "where canceller=:canceller");
    	query.setParameter("canceller", user);
    	query.setParameter("cancellerName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set closeInfo.user=null, "
    			+ "closeInfo.userName=:userName where closeInfo.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequest set lastUpdate.user=null, "
    			+ "lastUpdate.userName=:userName where lastUpdate.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update PullRequestChange set user=null, userName=:userName where user=:user");
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
    	
    	query = getSession().createQuery("update CodeComment set lastUpdate.user=null, lastUpdate.userName=:userName "
    			+ "where lastUpdate.user=:user");
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
    	
    	query = getSession().createQuery("update Issue set lastUpdate.user=null, lastUpdate.userName=:userName "
    			+ "where lastUpdate.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update IssueComment set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update IssueChange set user=null, userName=:userName where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("userName", user.getDisplayName());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Project set owner=null where owner=:user");
    	query.setParameter("user", user);
    	query.executeUpdate();
    	
		dao.remove(user);
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
		criteria.add(Restrictions.not(Restrictions.eq("id", User.SYSTEM_ID)));
		criteria.setCacheable(true);
		return query(criteria);
	}
	
	@Override
	public int count() {
		return count(true);
	}

	@Sessional
    @Override
    public User findByEmail(String email) {
		EntityCriteria<User> criteria = newCriteria();
		criteria.add(Restrictions.ilike(User.PROP_EMAIL, email));
		criteria.setCacheable(true);
		return find(criteria);
    }
	
    @Sessional
    @Override
    public User find(PersonIdent person) {
    	if (StringUtils.isNotBlank(person.getEmailAddress()))
    		return findByEmail(person.getEmailAddress());
    	else
    		return null;
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