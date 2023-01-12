package io.onedev.server.entitymanager.impl;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.hazelcast.core.HazelcastInstance;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.util.facade.GroupCache;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultGroupManager extends BaseEntityManager<Group> implements GroupManager {

	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	private final IssueFieldManager issueFieldManager;
	
    private final ClusterManager clusterManager;
    
    private final TransactionManager transactionManager;
    
	private volatile GroupCache cache;
	
	@Inject
	public DefaultGroupManager(Dao dao, ProjectManager projectManager, SettingManager settingManager, 
			IssueFieldManager issueFieldManager, ClusterManager clusterManager, 
			TransactionManager transactionManager) {
		super(dao);
		this.projectManager = projectManager;
		this.settingManager = settingManager;
		this.issueFieldManager = issueFieldManager;
		this.clusterManager = clusterManager;
		this.transactionManager = transactionManager;
	}
	
    @Sessional
    @Listen
    public void on(SystemStarted event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
        cache = new GroupCache(hazelcastInstance.getReplicatedMap("groupCache"));
        
    	for (Group group: query()) 
    		cache.put(group.getId(), group.getFacade());
    }
	
    @Transactional
    @Listen
    public void on(EntityRemoved event) {
    	if (event.getEntity() instanceof Group) {
    		Long id = event.getEntity().getId();
    		transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
			    	cache.remove(id);
				}
				
    		});
    	}
    }
    
    @Transactional
    @Listen
    public void on(EntityPersisted event) {
    	if (event.getEntity() instanceof Group) 
    		cacheAfterCommit((Group) event.getEntity());
    }
    
    private void cacheAfterCommit(Group group) {
    	GroupFacade facade = group.getFacade();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				if (cache != null)
					cache.put(facade.getId(), facade);
			}
			
		});
    }
    
	@Transactional
	@Override
	public void save(Group group, String oldName) {
		if (oldName != null && !oldName.equals(group.getName())) {
			for (Project project: projectManager.query()) {
				for (BranchProtection protection: project.getBranchProtections()) 
					protection.onRenameGroup(oldName, group.getName());
				for (TagProtection protection: project.getTagProtections())
					protection.onRenameGroup(oldName, group.getName());
			}
			
			settingManager.onRenameGroup(oldName, group.getName());
			issueFieldManager.onRenameGroup(oldName, group.getName());
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
    	Usage usage = new Usage();
		for (Project project: projectManager.query()) {
			Usage usedInProject = new Usage();
			for (BranchProtection protection: project.getBranchProtections()) 
				usedInProject.add(protection.onDeleteGroup(group.getName()));
			for (TagProtection protection: project.getTagProtections()) 
				usedInProject.add(protection.onDeleteGroup(group.getName()));
			usedInProject.prefix("project '" + project.getPath() + "': settings");
			usage.add(usedInProject);
		}

		usage.add(settingManager.onDeleteGroup(group.getName()));
		
		usage.checkInUse("Group '" + group.getName() + "'");
		
		dao.remove(group);
	}

	@Sessional
    @Override
    public Group find(String groupName) {
		GroupFacade facade = cache.find(groupName);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
    }

	@Override
	public List<Group> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}

	private EntityCriteria<Group> getCriteria(@Nullable String term) {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		if (term != null) 
			criteria.add(Restrictions.ilike("name", term, MatchMode.ANYWHERE));
		else
			criteria.setCacheable(true);
		return criteria;
	}
	
	@Sessional
	@Override
	public List<Group> query(String term, int firstResult, int maxResults) {
		EntityCriteria<Group> criteria = getCriteria(term);
		criteria.addOrder(Order.asc("name"));
		return query(criteria, firstResult, maxResults);
	}

	@Sessional
	@Override
	public int count(String term) {
		return count(getCriteria(term));
	}

	@Sessional
	@Override
	public List<Group> queryAdminstrator() {
		EntityCriteria<Group> criteria = EntityCriteria.of(Group.class);
		criteria.add(Restrictions.eq(Group.PROP_ADMINISTRATOR, true));
		criteria.setCacheable(true);
		return query(criteria);
	}

}
