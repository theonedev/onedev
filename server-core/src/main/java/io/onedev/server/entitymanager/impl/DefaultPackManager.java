package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.entitymanager.PackBlobAuthorizationManager;
import io.onedev.server.entitymanager.PackBlobReferenceManager;
import io.onedev.server.entitymanager.PackManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pack.PackPublished;
import io.onedev.server.model.*;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadPack;
import io.onedev.server.util.ProjectPackStats;
import io.onedev.server.util.criteria.Criteria;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static io.onedev.server.model.Pack.*;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Singleton
public class DefaultPackManager extends BaseEntityManager<Pack> 
		implements PackManager, Serializable {
	
	private final PackBlobAuthorizationManager blobAuthorizationManager;
	
	private final PackBlobReferenceManager blobReferenceManager;
	
	private final ProjectManager projectManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultPackManager(Dao dao, PackBlobAuthorizationManager blobAuthorizationManager,
							  PackBlobReferenceManager blobReferenceManager, 
							  ProjectManager projectManager, ListenerRegistry listenerRegistry) {
		super(dao);
		this.blobAuthorizationManager = blobAuthorizationManager;
		this.blobReferenceManager = blobReferenceManager;
		this.projectManager = projectManager;
		this.listenerRegistry = listenerRegistry;
	}

	private CriteriaQuery<Pack> buildCriteriaQuery(@Nullable Project project,
													Session session, EntityQuery<Pack> packQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Pack> query = builder.createQuery(Pack.class);
		Root<Pack> root = query.from(Pack.class);
		query.select(root);

		query.where(getPredicates(project, packQuery.getCriteria(), query, root, builder));

		applyOrders(root, query, builder, packQuery.getSorts());

		return query;
	}

	private void applyOrders(From<Pack, Pack> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder,
							 List<EntitySort> sorts) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: sorts) {
			if (sort.getDirection() == EntitySort.Direction.ASCENDING)
				orders.add(builder.asc(PackQuery.getPath(root, Pack.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(PackQuery.getPath(root, Pack.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get(Pack.PROP_ID)));
		criteriaQuery.orderBy(orders);
	}
	
	@Sessional
	@Override
	public List<Pack> query(Project project, EntityQuery<Pack> packQuery, int firstResult, int maxResults) {
		CriteriaQuery<Pack> criteriaQuery = buildCriteriaQuery(project, getSession(), packQuery);
		Query<Pack> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Sessional
	@Override
	public List<Pack> queryPrevComparables(Pack compareWith, String fuzzyQuery, int count) {
		Preconditions.checkState(compareWith.getBuild() != null);
		
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Pack> criteriaQuery = builder.createQuery(Pack.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);

		List<Predicate> predicates = new ArrayList<>();
		
		predicates.add(builder.equal(root.get(Pack.PROP_PROJECT), compareWith.getProject()));
		predicates.add(builder.equal(root.get(PROP_TYPE), compareWith.getType()));
		predicates.add(builder.equal(root.get(PROP_BUILD).get(Build.PROP_PROJECT), compareWith.getBuild().getProject()));		
		predicates.add(builder.lessThan(root.get(PROP_ID), compareWith.getId()));
		
		if (fuzzyQuery != null) {
			predicates.add(builder.like(
					builder.lower(root.get(PROP_TAG)), 
					"%" + fuzzyQuery.toLowerCase() + "%"));
		}

		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		applyOrders(root, criteriaQuery, builder, new ArrayList<>());			

		Query<Pack> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(Project project, Criteria<Pack> packCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);

		criteriaQuery.where(getPredicates(project, packCriteria, criteriaQuery, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public List<String> queryTags(Project project, String type, String lastTag, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root.get(PROP_TAG));
		criteriaQuery.where(
				builder.equal(root.get(PROP_PROJECT), project), 
				builder.equal(root.get(PROP_TYPE), type));
		criteriaQuery.orderBy(builder.asc(root.get(PROP_TAG)));
		
		if (lastTag != null) {
			var tags = getSession().createQuery(criteriaQuery).getResultList();
			var index = tags.indexOf(lastTag);
			if (index != -1) 
				return tags.subList(index + 1, min(tags.size(), index + 1 + count));
			else 
				return new ArrayList<>();
		} else {
			var query = getSession().createQuery(criteriaQuery);
			query.setMaxResults(count);
			return query.getResultList();
		}
	}

	@Sessional
	@Override
	public List<ProjectPackStats> queryStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectPackStats> criteriaQuery = builder.createQuery(ProjectPackStats.class);
			Root<Pack> root = criteriaQuery.from(Pack.class);
			criteriaQuery.multiselect(
					root.get(Pack.PROP_PROJECT).get(Project.PROP_ID),
					root.get(Pack.PROP_TYPE), builder.count(root));
			criteriaQuery.groupBy(root.get(PROP_PROJECT), root.get(Pack.PROP_TYPE));

			criteriaQuery.where(root.get(PROP_PROJECT).in(projects));
			criteriaQuery.orderBy(builder.asc(root.get(Pack.PROP_TYPE)));
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}
	
	private Predicate[] getPredicates(@Nullable Project project, @Nullable Criteria<Pack> criteria,
									  CriteriaQuery<?> query, From<Pack, Pack> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = getPredicates(project, root, builder);
		if (criteria != null)
			predicates.add(criteria.getPredicate(query, root, builder));
		return predicates.toArray(new Predicate[0]);
	}

	private Collection<Predicate> getPredicates(@Nullable Project project, From<Pack, Pack> root, 
												CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (project != null) {
			predicates.add(builder.equal(root.get(Pack.PROP_PROJECT), project));
		} else if (!SecurityUtils.isAdministrator()) {
			Collection<Project> projects = projectManager.getPermittedProjects(new ReadPack());
			if (!projects.isEmpty()) {
				Path<Long> projectIdPath = root.get(Pack.PROP_PROJECT).get(Project.PROP_ID);
				predicates.add(Criteria.forManyValues(builder, projectIdPath,
						projects.stream().map(it->it.getId()).collect(Collectors.toSet()),
						projectManager.getIds()));
			} else {
				predicates.add(builder.disjunction());
			}
		}
		return predicates;
	}

	@Sessional
	@Override
	public List<String> queryProps(Project project, String propName, String matchWith, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root.get(propName)).distinct(true);

		Collection<Predicate> predicates = getPredicates(project, root, builder);
		predicates.add(builder.like(
				builder.lower(root.get(propName)),
				"%" + matchWith.toLowerCase() + "%"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get(propName)));

		Query<String> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);

		return query.getResultList();
	}
	
	@Sessional
	@Override
	public Pack findByTag(Project project, String type, String tag) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.eq(PROP_TAG, tag));
		return find(criteria);
	}

	@Sessional
	@Override
	public Pack findByNameAndVersion(Project project, String type, String name, String version) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.ilike(PROP_NAME, name.toLowerCase()));
		criteria.add(Restrictions.ilike(PROP_VERSION, version.toLowerCase()));
		return find(criteria);
	}
	
	private EntityCriteria<Pack> newGroupCriteria(Project project, String type, String groupId) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.eq(PROP_GROUP_ID, groupId));
		return criteria;		
	}

	@Sessional
	@Override
	public Pack findByGWithoutAV(Project project, String type, String groupId) {
		var criteria = newGroupCriteria(project, type, groupId);
		criteria.add(Restrictions.isNull(PROP_ARTIFACT_ID));
		criteria.add(Restrictions.isNull(PROP_VERSION));
		return find(criteria);
	}

	@Sessional
	@Override
	public Pack findByGAV(Project project, String type, String groupId, String artifactId, String version) {
		var criteria = newGroupCriteria(project, type, groupId);
		criteria.add(Restrictions.eq(PROP_ARTIFACT_ID, artifactId));
		criteria.add(Restrictions.eq(PROP_VERSION, version));
		criteria.addOrder(org.hibernate.criterion.Order.desc(PROP_ID));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<Pack> queryByGAWithV(Project project, String type, String groupId, String artifactId) {
		var criteria = newGroupCriteria(project, type, groupId);
		criteria.add(Restrictions.eq(PROP_ARTIFACT_ID, artifactId));
		criteria.add(Restrictions.isNotNull(PROP_VERSION));
		var packs = query(criteria).stream().sorted(comparing(Pack::getPublishDate)).collect(toList());
		Collections.reverse(packs);
		var gavs = new HashSet<Triple<String, String, String>>();
		for (var it = packs.iterator(); it.hasNext();) {
			var pack = it.next();
			if (!gavs.add(new ImmutableTriple<>(pack.getGroupId(), pack.getArtifactId(), pack.getVersion())))
				it.remove();
		}
		Collections.reverse(packs);
		return packs;
	}

	@Sessional
	@Override
	public List<Pack> queryByName(Project project, String type, String name, 
								  Comparator<Pack> sortComparator) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.ilike(PROP_NAME, name.toLowerCase()));
		if (sortComparator == null)
			criteria.addOrder(org.hibernate.criterion.Order.asc(PROP_ID));
		var packs = query(criteria);
		if (sortComparator != null)
			packs.sort(sortComparator);
		return packs;
	}

	@Sessional
	@Override
	public List<Pack> queryLatests(Project project, String type, String nameQuery,
								   int firstResult, int maxResults) {
		Query<Pack> query = getSession().createQuery("" +
				"select p1 from Pack p1 " +
				"left outer join Pack p2 " +
				"	on p1.name = p2.name and p1.id < p2.id " +
				"where p2.id is null and lower(p1.name) like :name " +
				"order by p1.name");
		query.setParameter("name", "%" + nameQuery + "%");
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.list();
	}

	@Sessional
	@Override
	public int countNames(Project project, String type, String nameQuery, String excludeVersionQuery) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(builder.countDistinct(root.get(PROP_NAME)));

		var predicates = new ArrayList<Predicate>();
		if (nameQuery != null)
			predicates.add(builder.like(builder.lower(root.get(PROP_NAME)), "%" + nameQuery.toLowerCase() + "%"));
		if (excludeVersionQuery != null)
			predicates.add(builder.notLike(builder.lower(root.get(PROP_VERSION)), "%" + excludeVersionQuery + "%"));
		
		if (!predicates.isEmpty())
			criteriaQuery.where(predicates.toArray(new Predicate[0]));

		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public List<String> queryNames(Project project, String type, String nameQuery, String excludeVersionQuery, 
								   int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root.get(PROP_NAME)).distinct(true);

		var predicates = new ArrayList<Predicate>();
		if (nameQuery != null)
			predicates.add(builder.like(builder.lower(root.get(PROP_NAME)), "%" + nameQuery.toLowerCase() + "%"));
		if (excludeVersionQuery != null)
			predicates.add(builder.notLike(builder.lower(root.get(PROP_VERSION)), "%" + excludeVersionQuery + "%"));

		if (!predicates.isEmpty())
			criteriaQuery.where(predicates.toArray(new Predicate[0]));

		var query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.list();
	}
	
	@Sessional
	@Override
	public Map<String, List<Pack>> loadPacks(List<String> names, String exludeVersionQuery, 
											 Comparator<Pack> sortComparator) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Pack> criteriaQuery = builder.createQuery(Pack.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root);

		var predicates = Lists.newArrayList(root.get(PROP_NAME).in(names));
		if (exludeVersionQuery != null)
			predicates.add(builder.notLike(builder.lower(root.get(PROP_VERSION)), "%" + exludeVersionQuery + "%"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		if (sortComparator == null)
			criteriaQuery.orderBy(builder.asc(root.get(PROP_ID)));

		var packs = new LinkedHashMap<String, List<Pack>>();
		for (var name: names)
			packs.put(name, new ArrayList<>());
		
		var query = getSession().createQuery(criteriaQuery);
		var result = query.list();
		if (sortComparator != null)
			result.sort(sortComparator);
		
		for (var pack: result) {
			var packsOfName = packs.get(pack.getName());
			if (packsOfName != null)
				packsOfName.add(pack);
		}
		
		return packs;
	}

	@Transactional
	@Override
	public void deleteByTag(Project project, String type, String tag) {
		var pack = findByTag(project, type, tag);
		if (pack != null)
			delete(pack);
	}

	@Transactional
	public void createOrUpdate(Pack pack, Collection<PackBlob> packBlobs, boolean postPublishEvent) {
		dao.persist(pack);
		if (packBlobs != null) {
			packBlobs = new HashSet<>(packBlobs);
			for (var packBlob: packBlobs) {
				blobAuthorizationManager.authorize(pack.getProject(), packBlob);
				if (pack.getBlobReferences().stream().noneMatch(it -> it.getPackBlob().equals(packBlob))) {
					var blobReference = new PackBlobReference();
					blobReference.setPack(pack);
					blobReference.setPackBlob(packBlob);
					blobReferenceManager.create(blobReference);
				}
			}
			for (var blobReference: pack.getBlobReferences()) {
				if (packBlobs.stream().noneMatch(it -> it.equals(blobReference.getPackBlob())))
					blobReferenceManager.delete(blobReference);
			}
		}
		
		if (postPublishEvent)
			listenerRegistry.post(new PackPublished(pack));
	}
	
	@Transactional
	@Override
	public void delete(Collection<Pack> packs) {
		for (var pack: packs)
			delete(pack);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(PackManager.class);
	}
	
}
