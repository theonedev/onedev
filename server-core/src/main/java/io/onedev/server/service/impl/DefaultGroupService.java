package io.onedev.server.service.impl;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;

import io.onedev.server.cluster.ClusterService;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.Group;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.code.BranchProtection;
import io.onedev.server.model.support.code.TagProtection;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.service.GroupService;
import io.onedev.server.service.IssueFieldService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.SettingService;
import io.onedev.server.util.facade.GroupCache;
import io.onedev.server.util.facade.GroupFacade;
import io.onedev.server.util.usage.Usage;

@Singleton
public class DefaultGroupService extends BaseEntityService<Group> implements GroupService {

	@Inject
	private ProjectService projectService;

	@Inject
	private SettingService settingService;

	@Inject
	private IssueFieldService issueFieldService;

	@Inject
    private ClusterService clusterService;

	@Inject
    private TransactionService transactionService;
    
	private volatile GroupCache cache;

    @Sessional
    @Listen
    public void on(SystemStarting event) {
		HazelcastInstance hazelcastInstance = clusterService.getHazelcastInstance();
        cache = new GroupCache(hazelcastInstance.getReplicatedMap("groupCache"));
		for (Group group: query())
			cache.put(group.getId(), group.getFacade());
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
			for (Project project: projectService.query()) {
				try {
					for (BranchProtection protection : project.getBranchProtections())
						protection.onRenameGroup(oldName, group.getName());
					for (TagProtection protection : project.getTagProtections())
						protection.onRenameGroup(oldName, group.getName());
				} catch (Exception e) {
					throw new RuntimeException("Error checking group reference in project '" + project.getPath() + "'", e);
				}
			}
			
			settingService.onRenameGroup(oldName, group.getName());
			issueFieldService.onRenameGroup(oldName, group.getName());
		}
		dao.persist(group);
	}

	@Transactional
	@Override
	public void delete(Group group) {
    	Usage usage = new Usage();
		for (Project project: projectService.query()) {
			try {
				Usage usageInProject = new Usage();
				for (BranchProtection protection : project.getBranchProtections())
					usageInProject.add(protection.onDeleteGroup(group.getName()));
				for (TagProtection protection : project.getTagProtections())
					usageInProject.add(protection.onDeleteGroup(group.getName()));
				usageInProject.prefix("project '" + project.getPath() + "': settings");
				usage.add(usageInProject);
			} catch (Exception e) {
				throw new RuntimeException("Error checking group reference in project '" + project.getPath() + "'", e);
			}
		}
		usage.add(settingService.onDeleteGroup(group.getName()));
		usage.checkInUse("Group '" + group.getName() + "'");

    	var query = getSession().createQuery("update SsoProvider set defaultGroup=null where defaultGroup=:group");
    	query.setParameter("group", group);
    	query.executeUpdate();

		dao.remove(group);
	}

	@Sessional
    @Override
    public Group find(String name) {
		GroupFacade facade = cache.find(name);
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
		criteria.add(eq(Group.PROP_ADMINISTRATOR, true));
		criteria.setCacheable(true);
		return query(criteria);
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Group) {
			var facade = ((Group) event.getEntity()).getFacade();
			transactionService.runAfterCommit(() -> cache.put(facade.getId(), facade));
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Group) {
			var id = event.getEntity().getId();
			transactionService.runAfterCommit(() -> cache.remove(id));
		}
	}
	
}
