package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarting;
import io.onedev.server.model.*;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessPack;
import io.onedev.server.security.permission.PackPermission;
import io.onedev.server.util.facade.PackFacade;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.util.*;

@Singleton
public class DefaultPackManager extends BaseEntityManager<Pack> implements PackManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPackManager.class);
	
	private final ClusterManager clusterManager;
	
	private final TransactionManager transactionManager;
	
	private final SettingManager settingManager;

	private volatile IMap<Long, PackFacade> cache;
	
	private volatile Map<Long, Collection<String>> packNames;
	
	@Inject
	public DefaultPackManager(Dao dao, ClusterManager clusterManager, TransactionManager transactionManager, 
							  SettingManager settingManager) {
		super(dao);
		this.clusterManager = clusterManager;
		this.transactionManager = transactionManager;
		this.settingManager = settingManager;
	}
	
	@Transactional
	@Override
	public void create(Pack pack) {
		Preconditions.checkState(pack.isNew());
		dao.persist(pack);
	}

	@Transactional
	@Override
	public void update(Pack pack) {
		Preconditions.checkState(!pack.isNew());
		dao.persist(pack);
	}

	@Listen
	public void on(SystemStarting event) {
		logger.info("Caching package info...");

		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
		cache = hazelcastInstance.getMap("packCache");
		packNames = hazelcastInstance.getMap("packNames");

		var packCacheInited = hazelcastInstance.getCPSubsystem().getAtomicLong("packCacheInited");
		clusterManager.init(packCacheInited, () -> {
			Query<?> query = dao.getSession().createQuery("select id, project.id, name from Pack");
			for (Object[] fields : (List<Object[]>) query.list()) {
				Long packId = (Long) fields[0];
				Long projectId = (Long) fields[1];
				String packName = (String) fields[2];
				cache.put(packId, new PackFacade(packId, projectId, packName));
				populatePackNames(projectId, packName);
			}
			return 1L;
		});
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Pack) {
			var facade = ((Pack) event.getEntity()).getFacade();
			transactionManager.runAfterCommit(() -> {
				cache.put(facade.getId(), facade);
				populatePackNames(facade.getProjectId(), facade.getName());
			});
		}
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
			Long projectId = project.getId();
			transactionManager.runAfterCommit(() -> {
				cache.removeAll(entry -> entry.getValue().getProjectId().equals(projectId));
				packNames.remove(projectId);
			});
		}
	}

	private void populatePackNames(Long projectId, String packName) {
		Collection<String> packNamesOfProject = packNames.get(projectId);
		if (packNamesOfProject == null)
			packNamesOfProject = new HashSet<>();
		packNamesOfProject.add(packName);
		packNames.put(projectId, packNamesOfProject);
	}

	@Override
	public Collection<String> getPackNames(@Nullable Project project) {
		Collection<String> packNames = new HashSet<>();
		for (Map.Entry<Long, Collection<String>> entry: this.packNames.entrySet()) {
			if (project == null || project.getId().equals(entry.getKey()))
				packNames.addAll(entry.getValue());
		}
		return packNames;
	}

	private void populateAccessiblePackNames(Collection<String> accessiblePackNames,
											Collection<String> availablePackNames, Role role) {
		for (String packName: availablePackNames) {
			if (role.implies(new PackPermission(packName, new AccessPack())))
				accessiblePackNames.add(packName);
		}
	}
	
	private Collection<String> getAccessiblePackNames(Project project) {
		Collection<String> accessiblePackNames = new HashSet<>();
		Collection<String> availablePackNames = packNames.get(project.getId());
		if (availablePackNames != null) {
			if (SecurityUtils.isAdministrator()) {
				accessiblePackNames.addAll(availablePackNames);
			} else {
				User user = SecurityUtils.getUser();
				if (user != null) {
					for (UserAuthorization authorization: user.getProjectAuthorizations()) {
						if (authorization.getProject().isSelfOrAncestorOf(project)) {
							populateAccessiblePackNames(accessiblePackNames, availablePackNames,
									authorization.getRole());
						}
					}

					Set<Group> groups = new HashSet<>(user.getGroups());
					Group defaultLoginGroup = settingManager.getSecuritySetting().getDefaultLoginGroup();
					if (defaultLoginGroup != null)
						groups.add(defaultLoginGroup);

					for (Group group: groups) {
						for (GroupAuthorization authorization: group.getAuthorizations()) {
							if (authorization.getProject().isSelfOrAncestorOf(project)) {
								populateAccessiblePackNames(accessiblePackNames, availablePackNames,
										authorization.getRole());
							}
						}
					}
				}

				Project current = project;
				do {
					Role defaultRole = current.getDefaultRole();
					if (defaultRole != null)
						populateAccessiblePackNames(accessiblePackNames, availablePackNames, defaultRole);
					current = current.getParent();
				} while (current != null);
			}
		}
		return accessiblePackNames;
	}
	
	private Predicate[] getPredicates(Project project, @Nullable String type, @Nullable String term, 
									  From<Pack, Pack> root, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get(Pack.PROP_PROJECT), project));
		if (type != null)
			predicates.add(builder.equal(root.get(Pack.PROP_TYPE), type));			
		if (term != null)
			predicates.add(builder.like(builder.lower(root.get(Pack.PROP_NAME)), "%" + term.toLowerCase() + "%"));
		if (!SecurityUtils.canManagePacks(project)) {
			Collection<String> accessiblePackNames = getAccessiblePackNames(project);
			Collection<String> availablePackNames = packNames.get(project.getId());
			if (availablePackNames != null && !accessiblePackNames.containsAll(availablePackNames)) {
				List<Predicate> packPredicates = new ArrayList<>();
				for (String packName: accessiblePackNames)
					packPredicates.add(builder.equal(root.get(Pack.PROP_NAME), packName));
				predicates.add(builder.or(packPredicates.toArray(new Predicate[0])));
			}
		}
		return predicates.toArray(new Predicate[0]);
	}
	
	@Sessional
	@Override
	public List<Pack> query(Project project, @Nullable String type, @Nullable String term, 
							int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Pack> criteriaQuery = builder.createQuery(Pack.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root).where(getPredicates(project, type, term, root, builder));
		
		Query<Pack> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(Project project, @Nullable String type, @Nullable String term) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(builder.count(root)).where(getPredicates(project, type, term, root, builder));
		
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Override
	public Pack find(Project project, String packName) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(Pack.PROP_PROJECT, project));
		criteria.add(Restrictions.ilike(Pack.PROP_NAME, packName));
		return find(criteria);
	}
}
