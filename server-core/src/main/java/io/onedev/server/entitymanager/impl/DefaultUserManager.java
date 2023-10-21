package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.exception.ServerNotReadyException;
import io.onedev.server.entitymanager.*;
import io.onedev.server.model.*;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.persistence.IdManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.security.permission.BasePermission;
import io.onedev.server.security.permission.ConfidentialIssuePermission;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.facade.UserCache;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.usage.Usage;
import org.apache.shiro.authc.credential.PasswordService;
import org.hibernate.ReplicationMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.onedev.server.model.User.*;
import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultUserManager extends BaseEntityManager<User> implements UserManager {
	
    private final ProjectManager projectManager;
    
    private final SettingManager settingManager;
    
    private final IssueFieldManager issueFieldManager;
    
    private final IdManager idManager;
    
    private final EmailAddressManager emailAddressManager;
    
    private final GroupManager groupManager;
    
    private final TransactionManager transactionManager;
    
    private final ClusterManager clusterManager;
	
	private final PasswordService passwordService;
    
	private volatile UserCache cache;
	
	private volatile IMap<String, Long> temporalAccessTokens;
	
	@Inject
    public DefaultUserManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
							  IssueFieldManager issueFieldManager, IdManager idManager, 
							  GroupManager groupManager, EmailAddressManager emailAddressManager, 
							  TransactionManager transactionManager, ClusterManager clusterManager, 
							  PasswordService passwordService) {
        super(dao);
        
        this.projectManager = projectManager;
        this.settingManager = settingManager;
        this.issueFieldManager = issueFieldManager;
        this.idManager = idManager;
        this.emailAddressManager = emailAddressManager;
        this.transactionManager = transactionManager;
        this.groupManager = groupManager;
        this.clusterManager = clusterManager;
		this.passwordService = passwordService;
    }

	@Transactional
	@Override
	public void replicate(User user) {
		getSession().replicate(user, ReplicationMode.OVERWRITE);
		idManager.useId(User.class, user.getId());
		var facade = user.getFacade();
		if (facade.getId() > 0)
			transactionManager.runAfterCommit(() -> cache.put(facade.getId(), facade));
	}
	
    @Transactional
    @Override
	public void update(User user, String oldName) {
		Preconditions.checkState(!user.isNew());
		
    	user.setName(user.getName().toLowerCase());
    	
    	dao.persist(user);

    	if (oldName != null && !oldName.equals(user.getName())) {
    		for (Project project: projectManager.query()) {
				try {
					for (BranchProtection protection : project.getBranchProtections())
						protection.onRenameUser(oldName, user.getName());
					for (TagProtection protection : project.getTagProtections())
						protection.onRenameUser(oldName, user.getName());
					project.getIssueSetting().onRenameUser(oldName, user.getName());
					project.getBuildSetting().onRenameUser(oldName, user.getName());
				} catch (Exception e) {
					throw new RuntimeException("Error checking user reference in project '" + project.getPath() + "'", e);
				}
    		}
    		settingManager.onRenameUser(oldName, user.getName());
    		issueFieldManager.onRenameUser(oldName, user.getName());
    	}
    }
    
	@Transactional
    @Override
    public void create(User user) {
		Preconditions.checkState(user.isNew());
		user.setName(user.getName().toLowerCase());
		dao.persist(user);
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
			try {
				Usage usageInProject = new Usage();
				for (BranchProtection protection : project.getBranchProtections())
					usageInProject.add(protection.onDeleteUser(user.getName()));
				for (TagProtection protection : project.getTagProtections())
					usageInProject.add(protection.onDeleteUser(user.getName()));
				usageInProject.add(project.getIssueSetting().onDeleteUser(user.getName()));
				usageInProject.add(project.getBuildSetting().onDeleteUser(user.getName()));
				usageInProject.prefix("project '" + project.getPath() + "': settings");
				usage.add(usageInProject);
			} catch (Exception e) {
				throw new RuntimeException("Error checking user reference in project '" + project.getPath() + "'", e);
			}
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
    	
    	query = getSession().createQuery("update PullRequest set lastActivity.user=:unknown where lastActivity.user=:user");
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
    	
    	query = getSession().createQuery("update CodeComment set lastActivity.user=:unknown where lastActivity.user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentReply set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update CodeCommentStatusChange set user=:unknown where user=:user");
    	query.setParameter("user", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Issue set submitter=:unknown where submitter=:submitter");
    	query.setParameter("submitter", user);
    	query.setParameter("unknown", getUnknown());
    	query.executeUpdate();
    	
    	query = getSession().createQuery("update Issue set lastActivity.user=:unknown where lastActivity.user=:user");
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
    }
	
	@Transactional
	@Override
	public void delete(Collection<User> users) {
		for (var user: users)
			delete(user);
	}

	@Transactional
	@Override
	public void useInternalAuthentication(Collection<User> users) {
		for (var user: users) {
			user.setPassword(passwordService.encryptPassword(CryptoUtils.generateSecret()));
			user.setSsoConnector(null);
		}
	}

	@Transactional
	@Override
	public void useExternalAuthentication(Collection<User> users) {
		for (var user: users) {
			user.setPassword(EXTERNAL_MANAGED);
			user.setSsoConnector(null);
		}
	}

	@Transactional
	@Override
	public void setAsGuest(Collection<User> users, boolean guest) {
		for (var user: users) {
			user.setGuest(guest);
			if (guest) {
				var query = getSession().createQuery("update Build set submitter=:unknown where submitter=:submitter");
				query.setParameter("submitter", user);
				query.setParameter("unknown", getUnknown());
				query.executeUpdate();

				query = getSession().createQuery("update Build set canceller=:unknown where canceller=:canceller");
				query.setParameter("canceller", user);
				query.setParameter("unknown", getUnknown());
				query.executeUpdate();

				query = getSession().createQuery("update PullRequest set submitter=:unknown where submitter=:submitter");
				query.setParameter("submitter", user);
				query.setParameter("unknown", getUnknown());
				query.executeUpdate();
				
				query = getSession().createQuery("delete from PullRequestAssignment where user=:user");
				query.setParameter("user", user);
				query.executeUpdate();
				
				query = getSession().createQuery("delete from IssueWork where user=:user");
				query.setParameter("user", user);
				query.executeUpdate();
				
				query = getSession().createQuery("delete from Stopwatch where user=:user");
				query.setParameter("user", user);
				query.executeUpdate();
			}
			dao.persist(user);
		}
	}

	@Sessional
    @Override
    public User findByName(String userName) {
		UserFacade facade = cache.findByName(userName);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
    }

    @Override
    public UserFacade findFacadeById(Long userId) {
		return cache.get(userId);
    }
	
	@Sessional
    @Override
    public User findByFullName(String fullName) {
		UserFacade facade = cache.findByFullName(fullName);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
    }

	@Sessional
	@Override
	public User findByPasswordResetCode(String passwordResetCode) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq("passwordResetCode", passwordResetCode));
		return find(criteria);
	}
	
	@Sessional
    @Override
    public User findByAccessToken(String accessTokenValue) {
		if (cache != null) {
			UserFacade facade = cache.findByAccessToken(accessTokenValue);
			if (facade != null) {
				return load(facade.getId());
			} else {
				Long userId = temporalAccessTokens.get(accessTokenValue);
				if (userId != null)
					return load(userId);
				else
					return null;
			}
		} else {
			throw new ServerNotReadyException();
		}
    }
	
	@Override
	public String createTemporalAccessToken(Long userId, long secondsToExpire) {
		var value = CryptoUtils.generateSecret();
		temporalAccessTokens.put(value, userId, secondsToExpire, TimeUnit.SECONDS);
		return value;
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
		return cache.size();
	}

    @Sessional
    @Listen
    public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		temporalAccessTokens = hazelcastInstance.getMap("temporalAccessTokens");
		
		// Use replicated map, otherwise it will be slow to display many user avatars
		// (in issue list for instance) which will call findFacadeById many times
        cache = new UserCache(hazelcastInstance.getReplicatedMap("userCache"));
		for (User user: query())
			cache.put(user.getId(), user.getFacade());
    }
    
	@Transactional
	@Override
	public void onRenameSsoConnector(String oldName, String newName) {
    	Query<?> query = getSession().createQuery(String.format("update User set %s=:newName "
    			+ "where %s=:oldName", PROP_SSO_CONNECTOR, PROP_SSO_CONNECTOR));
    	query.setParameter("oldName", oldName);
    	query.setParameter("newName", newName);
    	query.executeUpdate();
	}

	@Transactional
	@Override
	public void onDeleteSsoConnector(String name) {
    	Query<?> query = getSession().createQuery(String.format("update User set %s=null, %s='12345' "
    			+ "where %s=:name", 
    			PROP_SSO_CONNECTOR, PROP_PASSWORD, PROP_SSO_CONNECTOR));
    	query.setParameter("name", name);
    	query.executeUpdate();
	}

	private Predicate[] getPredicates(CriteriaBuilder builder, CriteriaQuery<?> query, 
			Root<User> root, String term) {
		if (term != null) {
			term = "%" + term.toLowerCase() + "%";
			
			Subquery<EmailAddress> addressQuery = query.subquery(EmailAddress.class);
			Root<EmailAddress> addressRoot = addressQuery.from(EmailAddress.class);
			addressQuery.select(addressRoot);
			
			Predicate ownerPredicate = builder.equal(addressRoot.get(EmailAddress.PROP_OWNER), root);
			Predicate valuePredicate = builder.like(addressRoot.get(EmailAddress.PROP_VALUE), term);
			return new Predicate[] {
					builder.gt(root.get(AbstractEntity.PROP_ID), 0),
					builder.or(
							builder.like(root.get(User.PROP_NAME), term), 
							builder.like(builder.lower(root.get(User.PROP_FULL_NAME)), term), 
							builder.exists(addressQuery.where(ownerPredicate, valuePredicate)))};
		} else {
			return new Predicate[] {builder.gt(root.get(AbstractEntity.PROP_ID), 0)};
		}
	}
	
	@Override
	public List<User> query(String term, int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
		Root<User> root = criteriaQuery.from(User.class);
		
		criteriaQuery.where(getPredicates(builder, criteriaQuery, root, term));
		criteriaQuery.orderBy(builder.asc(root.get(User.PROP_NAME)));
		
		Query<User> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(String term) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<User> root = criteriaQuery.from(User.class);
		
		criteriaQuery.select(builder.count(root));
		criteriaQuery.where(getPredicates(builder, criteriaQuery, root, term));

		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public int countNonGuests() {
		return (int) cache.values().stream().filter(it -> !it.isGuest()).count();
	}

	@Sessional
	@Override
	public User findByVerifiedEmailAddress(String emailAddressValue) {
		EmailAddress emailAddress = emailAddressManager.findByValue(emailAddressValue);
		if (emailAddress != null && emailAddress.isVerified())
			return emailAddress.getOwner();
		else
			return null;
	}

	@Override
	public UserCache cloneCache() {
		return cache.clone();
	}

	private Collection<User> filterApplicable(Collection<User> users, BasePermission permission) {
		return users.stream().filter(permission::isApplicable).collect(toList());
	}
	
	@Override
	public Collection<User> getAuthorizedUsers(Project project, BasePermission permission) {
		UserCache cacheClone = cache.clone();

		Collection<User> authorizedUsers = Sets.newHashSet(getRoot());

		Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
   		if (defaultLoginGroup != null && defaultLoginGroup.isAdministrator())
   			return filterApplicable(cacheClone.getUsers(), permission);
   		
		for (Group group: groupManager.queryAdminstrator()) 
			authorizedUsers.addAll(group.getMembers());
		
		Project current = project;
		do {
			if (current.getDefaultRole() != null && current.getDefaultRole().implies(permission)) 
	   			return filterApplicable(cacheClone.getUsers(), permission);
			
			for (UserAuthorization authorization: current.getUserAuthorizations()) {  
				if (authorization.getRole().implies(permission))
					authorizedUsers.add(authorization.getUser());
			}
			
			for (GroupAuthorization authorization: current.getGroupAuthorizations()) {  
				if (authorization.getRole().implies(permission)) {
					if (authorization.getGroup().equals(defaultLoginGroup))
						return filterApplicable(cacheClone.getUsers(), permission);
					for (User user: authorization.getGroup().getMembers())
						authorizedUsers.add(user);
				}
			}
			current = current.getParent();
		} while (current != null);
		
		if (permission instanceof ConfidentialIssuePermission) {
			ConfidentialIssuePermission confidentialIssuePermission = (ConfidentialIssuePermission) permission;
			for (IssueAuthorization authorization: confidentialIssuePermission.getIssue().getAuthorizations()) 
				authorizedUsers.add(authorization.getUser());
		}

        return filterApplicable(authorizedUsers, permission);	
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		// Cache will be null when we run reset-admin-password command
		if (cache != null && event.getEntity() instanceof User) {
			var facade = (UserFacade) event.getEntity().getFacade();
			if (facade.getId() > 0)
				transactionManager.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		// Cache will be null when we run reset-admin-password command
		if (cache != null && event.getEntity() instanceof User) {
			var id = event.getEntity().getId();
			transactionManager.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}