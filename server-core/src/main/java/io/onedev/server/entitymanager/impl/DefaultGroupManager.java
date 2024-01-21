package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.IAtomicLong;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.IssueFieldManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static io.onedev.server.model.Group.PROP_NAME;
import static org.hibernate.criterion.Restrictions.eq;

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
    public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
        cache = new GroupCache(hazelcastInstance.getMap("groupCache"));

		IAtomicLong cacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("groupCacheInited");
		clusterManager.init(cacheInited, () -> {
			for (Group group: query())
				cache.put(group.getId(), group.getFacade());
			return 1L;
		});
    }
    
	@Transactional
	@Override
	public void create(Group group) {
		Preconditions.checkState(group.isNew());
		dao.persist(group);
	}

	@Transactional
	@Override
	public void update(Group group, String oldName) {
		Preconditions.checkState(!group.isNew());
		if (oldName != null && !oldName.equals(group.getName())) {
			for (Project project: projectManager.query()) {
				try {
					for (BranchProtection protection : project.getBranchProtections())
						protection.onRenameGroup(oldName, group.getName());
					for (TagProtection protection : project.getTagProtections())
						protection.onRenameGroup(oldName, group.getName());
					project.getBuildSetting().onRenameGroup(oldName, group.getName());
				} catch (Exception e) {
					throw new RuntimeException("Error checking group reference in project '" + project.getPath() + "'", e);
				}
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
			try {
				Usage usageInProject = new Usage();
				for (BranchProtection protection : project.getBranchProtections())
					usageInProject.add(protection.onDeleteGroup(group.getName()));
				for (TagProtection protection : project.getTagProtections())
					usageInProject.add(protection.onDeleteGroup(group.getName()));
				project.getBuildSetting().onDeleteGroup(group.getName());
				usageInProject.prefix("project '" + project.getPath() + "': settings");
				usage.add(usageInProject);
			} catch (Exception e) {
				throw new RuntimeException("Error checking group reference in project '" + project.getPath() + "'", e);
			}
		}
		usage.add(settingManager.onDeleteGroup(group.getName()));
		usage.checkInUse("Group '" + group.getName() + "'");

		dao.remove(group);
	}

	@Sessional
    @Override
    public Group find(String name) {
		/*
		GroupFacade facade = cache.find(name);
		if (facade != null)
			return load(facade.getId());
		else
			return null;
		 */
		return find(newCriteria().add(eq(PROP_NAME, name)));
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
		criteria.add(eq(Group.PROP_ADMINISTRATOR, true));
		criteria.setCacheable(true);
		return query(criteria);
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Group) {
			var facade = (GroupFacade) event.getEntity().getFacade();
			transactionManager.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Group) {
			var id = event.getEntity().getId();
			transactionManager.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}
