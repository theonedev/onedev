package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.event.build.BuildSubmitted;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.search.entity.build.BuildQueryBuildContext;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.BuildConstants;

@Singleton
public class DefaultBuildManager extends AbstractEntityManager<Build> implements BuildManager {

	private final BuildParamManager buildParamManager;
	
	private final BuildDependenceManager buildDependenceManager;
	
	private final StorageManager storageManager;
	
	@Inject
	public DefaultBuildManager(Dao dao, BuildParamManager buildParamManager, 
			BuildDependenceManager buildDependenceManager, 
			StorageManager storageManager) {
		super(dao);
		this.buildParamManager = buildParamManager;
		this.buildDependenceManager = buildDependenceManager;
		this.storageManager = storageManager;
	}

	@Transactional
	@Override
	public void delete(Build build) {
    	Query<?> query = getSession().createQuery("update PullRequestBuild set build=null where build=:build");
    	query.setParameter("build", build);
    	query.executeUpdate();
    	
		FileUtils.deleteDir(storageManager.getBuildDir(build.getProject().getId(), build.getId()));
    	super.delete(build);
	}
	
	@Sessional
	@Override
	public Build find(Project project, long number) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("number", number));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Sessional
	@Override
	public List<Build> query(Project project, String commitHash) {
		return query(project, commitHash, null, new HashMap<>());
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, String commitHash, String jobName, Map<String, String> params) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> restrictions = new ArrayList<>();
		restrictions.add(builder.equal(root.get("project"), project));
		restrictions.add(builder.equal(root.get("commitHash"), commitHash));
		if (jobName != null)
			restrictions.add(builder.equal(root.get("jobName"), jobName));
		
		for (Map.Entry<String, String> entry: params.entrySet()) {
			Join<?, ?> join = root.join("params", JoinType.INNER);
			restrictions.add(builder.equal(join.get("name"), entry.getKey()));
			restrictions.add(builder.equal(join.get("value"), entry.getValue()));
		}
		return getSession().createQuery(query.where(restrictions.toArray(new Predicate[0]))).list();
	}

	@Sessional
	@Override
	public List<Build> queryUnfinished() {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.or(
				Restrictions.eq("status", Status.QUEUEING), 
				Restrictions.eq("status", Status.RUNNING), 
				Restrictions.eq("status", Status.WAITING)));
		criteria.setCacheable(true);
		return query(criteria);
	}

	@Sessional
	@Override
	public List<Build> query(Project project, String term, int count) {
		List<Build> builds = new ArrayList<>();

		EntityCriteria<Build> criteria = newCriteria();
		
		if (term == null)
			term = "";
		
		if (term.startsWith("#")) {
			term = term.substring(1);
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.ilike("version", "%#" + term + "%"));
			}
		} else {
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.ilike("version", "%" + term + "%"));
			}
		}
		
		criteria.addOrder(Order.desc("number"));
		builds.addAll(query(criteria, 0, count));
		
		return builds;
	}
	
	@Override
	public List<Build> queryAfter(Project project, Long afterBuildId, int count) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("project", project));
		criteria.addOrder(Order.asc("id"));
		if (afterBuildId != null)
			criteria.add(Restrictions.gt("id", afterBuildId));
		return query(criteria, 0, count);
	}
	
	@Transactional
	@Override
	public void create(Build build) {
		Preconditions.checkArgument(build.isNew());
		Query<?> query = getSession().createQuery("select max(number) from Build where project=:project");
		query.setParameter("project", build.getProject());
		build.setNumber(getNextNumber(build.getProject(), query));
		save(build);

		for (BuildParam param: build.getParams())
			buildParamManager.save(param);
		for (BuildDependence dependence: build.getDependencies())
			buildDependenceManager.save(dependence);
	}

	private Predicate[] getPredicates(io.onedev.server.search.entity.EntityCriteria<Build> criteria, Project project, 
			QueryBuildContext<Build> context, User user) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(context.getBuilder().equal(context.getRoot().get(BuildConstants.ATTR_PROJECT), project));
		if (criteria != null)
			predicates.add(criteria.getPredicate(project, context, user));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Build> buildCriteriaQuery(Session session, Project project, EntityQuery<Build> buildQuery, User user) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		query.select(root).distinct(true);
		
		QueryBuildContext<Build> context = new BuildQueryBuildContext(root, builder);
		query.where(getPredicates(buildQuery.getCriteria(), project, context, user));

		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: buildQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(BuildQuery.getPath(root, BuildConstants.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(BuildQuery.getPath(root, BuildConstants.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get("id")));
		query.orderBy(orders);
		
		return query;
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, User user, EntityQuery<Build> buildQuery, int firstResult,
			int maxResults) {
		CriteriaQuery<Build> criteriaQuery = buildCriteriaQuery(getSession(), project, buildQuery, user);
		Query<Build> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(Project project, User user, io.onedev.server.search.entity.EntityCriteria<Build> buildCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Build> root = criteriaQuery.from(Build.class);

		QueryBuildContext<Build> context = new BuildQueryBuildContext(root, builder);
		criteriaQuery.where(getPredicates(buildCriteria, project, context, user));

		criteriaQuery.select(builder.countDistinct(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}
	
	@Listen
	public void on(BuildSubmitted event) {
		Build build = event.getBuild();
		FileUtils.deleteDir(storageManager.getBuildDir(build.getProject().getId(), build.getId()));
	}
}
