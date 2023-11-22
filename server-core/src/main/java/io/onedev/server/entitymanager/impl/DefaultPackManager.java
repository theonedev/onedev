package io.onedev.server.entitymanager.impl;

import com.google.common.base.Preconditions;
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
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.*;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static io.onedev.server.model.Pack.*;
import static java.lang.Math.min;

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
	
	private EntityCriteria<Pack> newCriteria(Project project, String type, @Nullable String query) {
		EntityCriteria<Pack> criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		if (query != null)
			criteria.add(Restrictions.ilike(PROP_VERSION, query, MatchMode.ANYWHERE));
		return criteria;
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
					builder.lower(root.get(PROP_VERSION)), 
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
		criteriaQuery.select(root.get(PROP_VERSION));
		criteriaQuery.where(
				builder.equal(root.get(PROP_PROJECT), project), 
				builder.equal(root.get(PROP_TYPE), type), 
				builder.notLike(root.get(PROP_VERSION), "%:%"));
		criteriaQuery.orderBy(builder.asc(root.get(PROP_VERSION)));
		
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
	public List<String> queryVersions(Project project, String matchWith, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root.get(Pack.PROP_VERSION)).distinct(true);

		Collection<Predicate> predicates = getPredicates(project, root, builder);
		predicates.add(builder.like(
				builder.lower(root.get(Pack.PROP_VERSION)),
				"%" + matchWith.toLowerCase() + "%"));
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.orderBy(builder.asc(root.get(Pack.PROP_VERSION)));

		Query<String> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);

		return query.getResultList();
	}

	@Sessional
	@Override
	public Pack find(Project project, String type, String version) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.eq(PROP_VERSION, version));
		return find(criteria);
	}

	@Transactional
	@Override
	public void delete(Project project, String type, String version) {
		var pack = find(project, type, version);
		if (pack != null)
			delete(pack);
	}
	
	@Transactional
	@Override
	public void createOrUpdate(Pack pack, Collection<PackBlob> packBlobs) {
		dao.persist(pack);
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
