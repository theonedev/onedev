package io.onedev.server.service.impl;

import static io.onedev.server.model.AbstractEntity.PROP_ID;
import static io.onedev.server.model.Pack.PROP_BUILD;
import static io.onedev.server.model.Pack.PROP_NAME;
import static io.onedev.server.model.Pack.PROP_PRERELEASE;
import static io.onedev.server.model.Pack.PROP_PROJECT;
import static io.onedev.server.model.Pack.PROP_TYPE;
import static io.onedev.server.model.Pack.PROP_VERSION;
import static io.onedev.server.model.Pack.SORT_FIELDS;
import static io.onedev.server.search.entity.EntitySort.Direction.ASCENDING;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.shiro.subject.Subject;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.onedev.server.service.PackBlobReferenceService;
import io.onedev.server.service.PackBlobService;
import io.onedev.server.service.PackLabelService;
import io.onedev.server.service.PackService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.UserService;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.pack.PackPublished;
import io.onedev.server.model.Build;
import io.onedev.server.model.Pack;
import io.onedev.server.model.PackBlob;
import io.onedev.server.model.PackBlobReference;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.pack.PackQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.ReadPack;
import io.onedev.server.util.ProjectPackTypeStat;
import io.onedev.server.util.criteria.Criteria;

@Singleton
public class DefaultPackService extends BaseEntityService<Pack>
		implements PackService, Serializable {

	@Inject
	private PackBlobReferenceService blobReferenceManager;

	@Inject
	private PackBlobService blobService;

	@Inject
	private ProjectService projectService;

	@Inject
	private PackLabelService labelService;

	@Inject
	private UserService userService;

	@Inject
	private ListenerRegistry listenerRegistry;

	private CriteriaQuery<Pack> buildCriteriaQuery(Subject subject, Project project, 
			Session session, EntityQuery<Pack> packQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Pack> query = builder.createQuery(Pack.class);
		Root<Pack> root = query.from(Pack.class);
		query.select(root);

		query.where(getPredicates(subject, project, packQuery.getCriteria(), query, root, builder));

		applyOrders(root, query, builder, packQuery.getSorts());

		return query;
	}

	private void applyOrders(From<Pack, Pack> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder,
							 List<EntitySort> sorts) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: sorts) {
			if (sort.getDirection() == ASCENDING)
				orders.add(builder.asc(PackQuery.getPath(root, SORT_FIELDS.get(sort.getField()).getProperty())));
			else
				orders.add(builder.desc(PackQuery.getPath(root, SORT_FIELDS.get(sort.getField()).getProperty())));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get(Pack.PROP_ID)));
		criteriaQuery.orderBy(orders);
	}
	
	@Sessional
	@Override
	public List<Pack> query(Subject subject, Project project, EntityQuery<Pack> packQuery, 
							boolean loadLabelsAndBlobs, int firstResult, int maxResults) {
		CriteriaQuery<Pack> criteriaQuery = buildCriteriaQuery(subject, project, getSession(), packQuery);
		Query<Pack> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		var packs = query.getResultList();
		
		if (!packs.isEmpty() && loadLabelsAndBlobs) {
			labelService.populateLabels(packs);
			blobService.populateBlobs(packs);
		}
		return packs;
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
		predicates.add(builder.equal(root.get(PROP_NAME), compareWith.getName()));
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
	public int count(Subject subject, Project project, Criteria<Pack> packCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);

		criteriaQuery.where(getPredicates(subject, project, packCriteria, criteriaQuery, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public List<String> queryVersions(Project project, String type, String name, 
									  String lastVersion, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root.get(PROP_VERSION));
		criteriaQuery.where(
				builder.equal(root.get(PROP_PROJECT), project), 
				builder.equal(root.get(PROP_TYPE), type),
				builder.equal(root.get(PROP_NAME), name));
		criteriaQuery.orderBy(builder.asc(root.get(PROP_VERSION)));
		
		if (lastVersion != null) {
			var versions = getSession().createQuery(criteriaQuery).getResultList();
			var index = versions.indexOf(lastVersion);
			if (index != -1) 
				return versions.subList(index + 1, min(versions.size(), index + 1 + count));
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
	public List<ProjectPackTypeStat> queryTypeStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectPackTypeStat> criteriaQuery = builder.createQuery(ProjectPackTypeStat.class);
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
	
	private Predicate[] getPredicates(Subject subject, @Nullable Project project, @Nullable Criteria<Pack> criteria,
									  CriteriaQuery<?> query, From<Pack, Pack> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = getPredicates(subject, project, root, builder);
		if (criteria != null) 
			predicates.add(criteria.getPredicate(null, query, root, builder));
		return predicates.toArray(new Predicate[0]);
	}

	private Collection<Predicate> getPredicates(Subject subject, @Nullable Project project, From<Pack, Pack> root, 
												CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<>();
		if (project != null) {
			predicates.add(builder.equal(root.get(Pack.PROP_PROJECT), project));
		} else if (!SecurityUtils.isAdministrator(subject)) {
			Collection<Project> projects = SecurityUtils.getAuthorizedProjects(subject, new ReadPack());
			if (!projects.isEmpty()) {
				Path<Long> projectIdPath = root.get(Pack.PROP_PROJECT).get(Project.PROP_ID);
				predicates.add(Criteria.forManyValues(builder, projectIdPath,
						projects.stream().map(it->it.getId()).collect(Collectors.toSet()),
						projectService.getIds()));
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

		Collection<Predicate> predicates = getPredicates(userService.getSystem().asSubject(), project, root, builder);
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
	public Pack findByNameAndVersion(Project project, String type, String name, String version) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		criteria.add(Restrictions.ilike(PROP_NAME, name.toLowerCase()));
		criteria.add(Restrictions.ilike(PROP_VERSION, version.toLowerCase()));
		return find(criteria);
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
	public List<Pack> query(Project project, String type, Boolean includePrerelease) {
		var criteria = newCriteria();
		criteria.add(Restrictions.eq(PROP_PROJECT, project));
		criteria.add(Restrictions.eq(PROP_TYPE, type));
		if (includePrerelease != null)
			criteria.add(Restrictions.eq(PROP_PRERELEASE, includePrerelease));
		criteria.addOrder(org.hibernate.criterion.Order.asc(PROP_ID));
		return query(criteria);
	}
	
	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public List<Pack> queryLatests(Project project, String type, String nameQuery,
								   boolean includePrerelease, int firstResult, int maxResults) {
		var queryString = "" +
				"select p1 from Pack p1 " +
				"left outer join Pack p2 " +
				"	on p1.name = p2.name and p1.id < p2.id";
		if (!includePrerelease)
			queryString += " and p2.prerelease = false";
		queryString += " where p2.id is null";
		
		if (nameQuery != null) 
			queryString += " and lower(p1.name) like :name";		
		if (!includePrerelease)
			queryString += " and p1.prerelease = false";
		
		queryString += " order by p1.name";

		Query<Pack> query = getSession().createQuery(queryString);
		if (nameQuery != null)
			query.setParameter("name", "%" + nameQuery + "%");
		
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.list();
	}

	@Sessional
	@Override
	public int countNames(Project project, String type, String nameQuery, boolean includePrerelease) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(builder.countDistinct(root.get(PROP_NAME)));

		var predicates = new ArrayList<Predicate>();
		if (nameQuery != null)
			predicates.add(builder.like(builder.lower(root.get(PROP_NAME)), "%" + nameQuery.toLowerCase() + "%"));
		if (!includePrerelease)
			predicates.add(builder.equal(root.get(PROP_PRERELEASE), false));
		
		if (!predicates.isEmpty())
			criteriaQuery.where(predicates.toArray(new Predicate[0]));

		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}

	@Sessional
	@Override
	public List<String> queryNames(Project project, String type, String nameQuery, 
								   boolean includePrerelease, int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root.get(PROP_NAME)).distinct(true);

		var predicates = new ArrayList<Predicate>();
		if (nameQuery != null)
			predicates.add(builder.like(builder.lower(root.get(PROP_NAME)), "%" + nameQuery.toLowerCase() + "%"));
		if (!includePrerelease)
			predicates.add(builder.equal(root.get(PROP_PRERELEASE), false));

		if (!predicates.isEmpty())
			criteriaQuery.where(predicates.toArray(new Predicate[0]));

		var query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.list();
	}
	
	@Sessional
	@Override
	public Map<String, List<Pack>> loadPacks(List<String> names, boolean includePrerelease, 
											 Comparator<Pack> sortComparator) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Pack> criteriaQuery = builder.createQuery(Pack.class);
		Root<Pack> root = criteriaQuery.from(Pack.class);
		criteriaQuery.select(root);

		var predicates = Lists.newArrayList(root.get(PROP_NAME).in(names));
		if (!includePrerelease)
			predicates.add(builder.equal(root.get(PROP_PRERELEASE), false));
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
	public void deleteByNameAndVersion(Project project, String type, String name, String version) {
		var pack = findByNameAndVersion(project, type, name, version);
		if (pack != null)
			delete(pack);
	}

	@Transactional
	@Override
	public void createOrUpdate(Pack pack, Collection<PackBlob> packBlobs, boolean postPublishEvent) {
		dao.persist(pack);
		if (packBlobs != null) {
			packBlobs = new HashSet<>(packBlobs);
			for (var packBlob: packBlobs) {
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

}
