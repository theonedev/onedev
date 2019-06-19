package io.onedev.server.entitymanager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.schedule.SchedulableTask;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.event.build.BuildSubmitted;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.BuildConstants;

@Singleton
public class DefaultBuildManager extends AbstractEntityManager<Build> implements BuildManager, SchedulableTask {

	private static final int STATUS_QUERY_BATCH = 500;
	
	private static final int CLEANUP_BATCH = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildManager.class);
	
	private final BuildParamManager buildParamManager;
	
	private final BuildDependenceManager buildDependenceManager;
	
	private final StorageManager storageManager;
	
	private final TaskScheduler taskScheduler;
	
	private final TransactionManager transactionManager;
	
	private String taskId;
	
	@Inject
	public DefaultBuildManager(Dao dao, BuildParamManager buildParamManager, 
			TaskScheduler taskScheduler, BuildDependenceManager buildDependenceManager, 
			StorageManager storageManager, TransactionManager transactionManager) {
		super(dao);
		this.buildParamManager = buildParamManager;
		this.buildDependenceManager = buildDependenceManager;
		this.storageManager = storageManager;
		this.taskScheduler = taskScheduler;
		this.transactionManager = transactionManager;
	}

	@Transactional
	@Override
	public void delete(Build build) {
		FileUtils.deleteDir(storageManager.getBuildDir(build.getProject().getId(), build.getNumber()));
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
	public Collection<Build> query(Project project, ObjectId commitId) {
		return query(project, commitId, null);
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName) {
		return query(project, commitId, jobName, new HashMap<>());
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName, Map<String, List<String>> params) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> restrictions = new ArrayList<>();
		restrictions.add(builder.equal(root.get("project"), project));
		restrictions.add(builder.equal(root.get("commitHash"), commitId.name()));
		if (jobName != null)
			restrictions.add(builder.equal(root.get("jobName"), jobName));
		
		for (Map.Entry<String, List<String>> entry: params.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				for (String value: entry.getValue()) {
					Join<?, ?> join = root.join(BuildConstants.ATTR_PARAMS, JoinType.INNER);
					restrictions.add(builder.equal(join.get(BuildParam.ATTR_NAME), entry.getKey()));
					restrictions.add(builder.equal(join.get(BuildParam.ATTR_VALUE), value));
				}
			} else {
				Join<?, ?> join = root.join(BuildConstants.ATTR_PARAMS, JoinType.INNER);
				restrictions.add(builder.equal(join.get(BuildParam.ATTR_NAME), entry.getKey()));
				restrictions.add(builder.isNull(join.get(BuildParam.ATTR_VALUE)));
			}
		}
		query.where(restrictions.toArray(new Predicate[0]));
		return getSession().createQuery(query).list();
	}

	@Sessional
	@Override
	public Collection<Build> queryUnfinished() {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.or(
				Restrictions.eq("status", Status.PENDING), 
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
		
		if (StringUtils.isNotBlank(term)) {
			if (term.startsWith("#")) {
				term = term.substring(1);
				try {
					long buildNumber = Long.parseLong(term);
					criteria.add(Restrictions.eq("number", buildNumber));
				} catch (NumberFormatException e) {
					criteria.add(Restrictions.ilike("version", "#" + term, MatchMode.ANYWHERE));
				}
			} else {
				try {
					long buildNumber = Long.parseLong(term);
					criteria.add(Restrictions.eq("number", buildNumber));
				} catch (NumberFormatException e) {
					criteria.add(Restrictions.ilike("version", term, MatchMode.ANYWHERE));
				}
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
			Root<Build> root, CriteriaBuilder builder, User user) {
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get(BuildConstants.ATTR_PROJECT), project));
		if (criteria != null)
			predicates.add(criteria.getPredicate(project, root, builder, user));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Build> buildCriteriaQuery(Session session, Project project, EntityQuery<Build> buildQuery, User user) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		query.select(root).distinct(true);
		
		query.where(getPredicates(buildQuery.getCriteria(), project, root, builder, user));

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

		criteriaQuery.where(getPredicates(buildCriteria, project, root, builder, user));

		criteriaQuery.select(builder.countDistinct(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}
	
	@Listen
	public void on(BuildSubmitted event) {
		Build build = event.getBuild();
		FileUtils.deleteDir(storageManager.getBuildDir(build.getProject().getId(), build.getNumber()));
	}

	@Sessional
	@Override
	public Map<ObjectId, Map<String, Status>> queryStatus(Project project, Collection<ObjectId> commitIds) {
		Map<ObjectId, Map<String, Collection<Status>>> commitStatuses = new HashMap<>();
		
		Collection<ObjectId> batch = new HashSet<>();
		for (ObjectId commitId: commitIds) {
			batch.add(commitId);
			if (batch.size() == STATUS_QUERY_BATCH) {
				fillStatus(project, batch, commitStatuses);
				batch.clear();
			}
		}
		if (!batch.isEmpty())
			fillStatus(project, batch, commitStatuses);
		Map<ObjectId, Map<String, Status>> overallCommitStatuses = new HashMap<>();
		for (Map.Entry<ObjectId, Map<String, Collection<Status>>> entry: commitStatuses.entrySet()) {
			Map<String, Status> jobOverallStatuses = new HashMap<>();
			for (Map.Entry<String, Collection<Status>> entry2: entry.getValue().entrySet()) 
				jobOverallStatuses.put(entry2.getKey(), Status.getOverallStatus(entry2.getValue()));
			overallCommitStatuses.put(entry.getKey(), jobOverallStatuses);
		}
		for (ObjectId commitId: commitIds) {
			if (!overallCommitStatuses.containsKey(commitId))
				overallCommitStatuses.put(commitId, new HashMap<>());
		}
		return overallCommitStatuses;
	}
	
	@SuppressWarnings("unchecked")
	private void fillStatus(Project project, Collection<ObjectId> commitIds, 
			Map<ObjectId, Map<String, Collection<Status>>> commitStatuses) {
		Query<?> query = getSession().createQuery("select commitHash, jobName, status from Build "
				+ "where project=:project and commitHash in :commitHashes");
		query.setParameter("project", project);
		query.setParameter("commitHashes", commitIds.stream().map(it->it.name()).collect(Collectors.toList()));
		for (Object[] row: (List<Object[]>)query.list()) {
			ObjectId commitId = ObjectId.fromString((String) row[0]);
			String jobName = (String) row[1];
			Status status = (Status) row[2];
			Map<String, Collection<Status>> commitStatus = commitStatuses.get(commitId);
			if (commitStatus == null) {
				commitStatus = new HashMap<>();
				commitStatuses.put(commitId, commitStatus);
			}
			Collection<Status> jobStatus = commitStatus.get(jobName);
			if (jobStatus == null) {
				jobStatus = new HashSet<>();
				commitStatus.put(jobName, jobStatus);
			}
			jobStatus.add(status);
		}
	}

	@Override
	public void execute() {
		EntityCriteria<Build> criteria = newCriteria();
		
		AtomicInteger firstResult = new AtomicInteger(0);
		Map<Long, Optional<BuildQuery>> preserveConditions = new HashMap<>(); 
		
		while (transactionManager.call(new Callable<Boolean>() {

			@Override
			public Boolean call() {
				List<Build> builds = query(criteria, firstResult.get(), CLEANUP_BATCH);
				if (!builds.isEmpty()) {
					logger.debug("Checking build preserve condition: {}->{}", 
							firstResult.get()+1, firstResult.get()+builds.size());
				}
				for (Build build: builds) {
					Project project = build.getProject();
					Optional<BuildQuery> query = preserveConditions.get(project.getId());
					if (query == null) {
						try {
							String queryString = project.getBuildSetting().getBuildsToPreserve();
							query = Optional.of(BuildQuery.parse(project, queryString, true));
							if (query.get().needsLogin())
								throw new OneException("This query needs login which is not supported here");
						} catch (Exception e) {
							logger.error("Error parsing build preserve condition of project '{}'", project.getName(), e);
							query = Optional.absent();
						}
						preserveConditions.put(project.getId(), query);
					}
					if (query.isPresent()) {
						if (!query.get().matches(build, null)) {
							logger.debug("Preserve condition not satisfied, deleting build {}...", build.getId());
							delete(build);
						}
					}
				}
				firstResult.set(firstResult.get() + CLEANUP_BATCH);
				return builds.size() == CLEANUP_BATCH;
			}
			
		})) {}
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}
	
	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}
	
}
