package io.onedev.server.entitymanager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.search.entity.EntityQuery;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.build.BuildQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuild;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultBuildManager extends BaseEntityManager<Build> implements BuildManager, SchedulableTask {

	private static final int STATUS_QUERY_BATCH = 500;
	
	private static final int CLEANUP_BATCH = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildManager.class);
	
	private final BuildParamManager buildParamManager;
	
	private final BuildDependenceManager buildDependenceManager;
	
	private final StorageManager storageManager;
	
	private final GroupManager groupManager;
	
	private final ProjectManager projectManager;
	
	private final TaskScheduler taskScheduler;
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	private final Map<Long, BuildFacade> builds = new HashMap<>();
	
	private final ReadWriteLock buildsLock = new ReentrantReadWriteLock();
	
	private final Map<Long, Collection<String>> jobNames = new HashMap<>();
	
	private final ReadWriteLock jobNamesLock = new ReentrantReadWriteLock();
	
	private String taskId;
	
	@Inject
	public DefaultBuildManager(Dao dao, BuildParamManager buildParamManager, 
			TaskScheduler taskScheduler, BuildDependenceManager buildDependenceManager,
			GroupManager groupManager, StorageManager storageManager, 
			ProjectManager projectManager, SessionManager sessionManager, 
			TransactionManager transactionManager) {
		super(dao);
		this.buildParamManager = buildParamManager;
		this.buildDependenceManager = buildDependenceManager;
		this.storageManager = storageManager;
		this.groupManager = groupManager;
		this.projectManager = projectManager;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
	}

	@Transactional
	@Override
	public void delete(Build build) {
    	super.delete(build);
    	
		FileUtils.deleteDir(storageManager.getBuildDir(build.getProject().getId(), build.getNumber()));
		Long buildId = build.getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				buildsLock.writeLock().lock();
				try {
					builds.remove(buildId);
				} finally {
					buildsLock.writeLock().unlock();
				}
			}
		});
	}
	
	@Sessional
	@Override
	public Build find(Project project, long number) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq(Build.PROP_NUMBER_SCOPE, project.getForkRoot()));
		criteria.add(Restrictions.eq(Build.PROP_NUMBER, number));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Build find(String buildFQN) {
		return find(ProjectScopedNumber.from(buildFQN));
	}

	@Sessional
	@Override
	public Build find(ProjectScopedNumber buildFQN) {
		return find(buildFQN.getProject(), buildFQN.getNumber());
	}
	
	@Transactional
	@Override
	public void save(Build build) {
		super.save(build);
		
		BuildFacade facade = build.getFacade();
		String jobName = build.getJobName();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				buildsLock.writeLock().lock();
				try {
					builds.put(facade.getId(), facade);
				} finally {
					buildsLock.writeLock().unlock();
				}
				jobNamesLock.writeLock().lock();
				try {
					populateJobNames(facade.getProjectId(), jobName);
				} finally {
					jobNamesLock.writeLock().unlock();
				}
			}
			
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					buildsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, BuildFacade>> it = builds.entrySet().iterator(); it.hasNext();) {
							BuildFacade build = it.next().getValue();
							if (build.getProjectId().equals(projectId))
								it.remove();
						}
					} finally {
						buildsLock.writeLock().unlock();
					}
					jobNamesLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, Collection<String>>> it = jobNames.entrySet().iterator(); it.hasNext();) {
							if (it.next().getKey().equals(projectId))
								it.remove();
						}
					} finally {
						jobNamesLock.writeLock().unlock();
					}
				}
			});
		}
	}

	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId) {
		return query(project, commitId, null);
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName) {
		return query(project, commitId, jobName, null, null, new HashMap<>());
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName, 
			String refName, Optional<PullRequest> request, Map<String, List<String>> params) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get(Build.PROP_PROJECT), project));
		predicates.add(builder.equal(root.get(Build.PROP_COMMIT), commitId.name()));
		if (jobName != null)
			predicates.add(builder.equal(root.get(Build.PROP_JOB), jobName));
		if (refName != null)
			predicates.add(builder.equal(root.get(Build.PROP_REF_NAME), refName));

		if (request != null) {
			if (request.isPresent())
				predicates.add(builder.equal(root.get(Build.PROP_PULL_REQUEST), request.get()));
			else
				predicates.add(builder.isNull(root.get(Build.PROP_PULL_REQUEST)));
		}
			
		for (Map.Entry<String, List<String>> entry: params.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				for (String value: entry.getValue()) {
					Join<?, ?> join = root.join(Build.PROP_PARAMS, JoinType.INNER);
					predicates.add(builder.equal(join.get(BuildParam.PROP_NAME), entry.getKey()));
					predicates.add(builder.equal(join.get(BuildParam.PROP_VALUE), value));
				}
			} else {
				Join<?, ?> join = root.join(Build.PROP_PARAMS, JoinType.INNER);
				predicates.add(builder.equal(join.get(BuildParam.PROP_NAME), entry.getKey()));
				predicates.add(builder.isNull(join.get(BuildParam.PROP_VALUE)));
			}
		}
		
		query.where(predicates.toArray(new Predicate[0]));
		return getSession().createQuery(query).list();
	}

	@Sessional
	@Override
	public Collection<Build> queryUnfinished() {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.or(
				Restrictions.eq(Build.PROP_STATUS, Status.PENDING), 
				Restrictions.eq(Build.PROP_STATUS, Status.RUNNING), 
				Restrictions.eq(Build.PROP_STATUS, Status.WAITING)));
		criteria.setCacheable(true);
		return query(criteria);
	}

	@Sessional
	@Override
	public List<Build> query(Project project, String term, int count) {
		List<Build> builds = new ArrayList<>();

		EntityCriteria<Build> criteria = newCriteria();
		
		Set<Project> projects = Sets.newHashSet(project);
		projects.addAll(project.getForkParents());

		List<Criterion> projectCriterions = new ArrayList<>();
		jobNamesLock.readLock().lock();
		try {
			for (Project each: projects) {
				Collection<String> availableJobNames = jobNames.get(each.getId());
				if (!availableJobNames.isEmpty()) {
					Collection<String> accessibleJobNames = getAccessibleJobNames(each);
					if (accessibleJobNames.containsAll(availableJobNames)) {
						projectCriterions.add(Restrictions.eq(Build.PROP_PROJECT, each));
					} else {
						List<Criterion> jobCriterions = new ArrayList<>();
						for (String jobName: accessibleJobNames) 
							jobCriterions.add(Restrictions.eq(Build.PROP_JOB, jobName));
						if (!jobCriterions.isEmpty()) {
							projectCriterions.add(Restrictions.and(
									Restrictions.eq(Build.PROP_PROJECT, each), 
									Restrictions.or(jobCriterions.toArray(new Criterion[0]))));
						}
					}
				}
			}
		} finally {
			jobNamesLock.readLock().unlock();
		}
		
		if (!projectCriterions.isEmpty()) {
			criteria.add(Restrictions.or(projectCriterions.toArray(new Criterion[0])));
			
			if (term.startsWith("#"))
				term = term.substring(1);
			if (term.length() != 0) {
				try {
					long buildNumber = Long.parseLong(term);
					criteria.add(Restrictions.eq(Build.PROP_NUMBER, buildNumber));
				} catch (NumberFormatException e) {
					criteria.add(Restrictions.or(
							Restrictions.ilike(Build.PROP_VERSION, term, MatchMode.ANYWHERE),
							Restrictions.ilike(Build.PROP_JOB, term, MatchMode.ANYWHERE)));
				}
			}

			criteria.addOrder(Order.desc(Build.PROP_PROJECT));
			criteria.addOrder(Order.desc(Build.PROP_NUMBER));
			builds.addAll(query(criteria, 0, count));
		} 

		return builds;
	}
	
	@Sessional
	@Override
	public List<String> queryVersions(Project project, String matchWith, int count) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<String> criteriaQuery = builder.createQuery(String.class);
		Root<Build> root = criteriaQuery.from(Build.class);
		criteriaQuery.select(root.get(Build.PROP_VERSION)).distinct(true);
		
		Collection<Predicate> predicates = getPredicates(project, root, builder);
		predicates.add(builder.like(
				builder.lower(root.get(Build.PROP_VERSION)), 
				"%" + matchWith.toLowerCase() + "%"));
		criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
		criteriaQuery.orderBy(builder.asc(root.get(Build.PROP_VERSION)));

		Query<String> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(count);
		
		return query.getResultList();
	}
	
	@Transactional
	@Override
	public void create(Build build) {
		Preconditions.checkArgument(build.isNew());
		build.setNumberScope(build.getProject().getForkRoot());
		Query<?> query = getSession().createQuery(String.format("select max(%s) from Build where %s=:numberScope", 
				Build.PROP_NUMBER, Build.PROP_NUMBER_SCOPE));
		query.setParameter("numberScope", build.getNumberScope());
		build.setNumber(getNextNumber(build.getNumberScope(), query));
		save(build);
		for (BuildParam param: build.getParams())
			buildParamManager.save(param);
		for (BuildDependence dependence: build.getDependencies())
			buildDependenceManager.save(dependence);
	}

	private Collection<Predicate> getPredicates(@Nullable Project project, Root<Build> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = new ArrayList<>();

		jobNamesLock.readLock().lock();
		try {
			if (project != null) {
				predicates.add(builder.equal(root.get(Build.PROP_PROJECT), project));
				if (!SecurityUtils.canManageBuilds(project)) {
					Collection<String> accessibleJobNames = getAccessibleJobNames(project);
					Collection<String> availableJobNames = jobNames.get(project.getId());
					if (availableJobNames != null && !accessibleJobNames.containsAll(availableJobNames)) {
						List<Predicate> jobPredicates = new ArrayList<>();
						for (String jobName: accessibleJobNames) 
							jobPredicates.add(builder.equal(root.get(Build.PROP_JOB), jobName));
						predicates.add(builder.or(jobPredicates.toArray(new Predicate[jobPredicates.size()])));
					}
				}
			} else if (!SecurityUtils.isAdministrator()) {
				List<Predicate> projectPredicates = new ArrayList<>();
				Collection<Long> projectsWithAllJobs = new HashSet<>();
				for (Map.Entry<Project, Collection<String>> entry: getAccessibleJobNames().entrySet()) {
					project = entry.getKey();
					if (SecurityUtils.canManageBuilds(project)) {
						projectPredicates.add(builder.equal(root.get(Build.PROP_PROJECT), project));
						projectsWithAllJobs.add(project.getId());
					} else {
						Collection<String> availableJobNamesOfProject = jobNames.get(project.getId());
						if (availableJobNamesOfProject != null) {
							Collection<String> accessibleJobNamesOfProject = entry.getValue();
							if (accessibleJobNamesOfProject.containsAll(availableJobNamesOfProject)) {
								projectsWithAllJobs.add(project.getId());
								projectPredicates.add(builder.equal(root.get(Build.PROP_PROJECT), project));
							} else {
								List<Predicate> jobPredicates = new ArrayList<>();
								for (String jobName: accessibleJobNamesOfProject) 
									jobPredicates.add(builder.equal(root.get(Build.PROP_JOB), jobName));
								projectPredicates.add(builder.and(
										builder.equal(root.get(Build.PROP_PROJECT), project), 
										builder.or(jobPredicates.toArray(new Predicate[jobPredicates.size()]))));
							}
						} else {
							projectsWithAllJobs.add(project.getId());
						}
					}
				}
				if (!projectsWithAllJobs.containsAll(jobNames.keySet()))
					predicates.add(builder.or(projectPredicates.toArray(new Predicate[projectPredicates.size()])));
			}
		} finally {
			jobNamesLock.readLock().unlock();
		}
		
		return predicates;
	}
	
	private Predicate[] getPredicates(@Nullable Project project, 
			@Nullable io.onedev.server.search.entity.EntityCriteria<Build> criteria, 
			Root<Build> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = getPredicates(project, root, builder);
		if (criteria != null) 
			predicates.add(criteria.getPredicate(root, builder));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Build> buildCriteriaQuery(@Nullable Project project, 
			Session session,  EntityQuery<Build> buildQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		query.select(root);
		
		query.where(getPredicates(project, buildQuery.getCriteria(), root, builder));

		applyOrders(root, query, builder, buildQuery);
		
		return query;
	}
	
	@Sessional
	@Override
	public List<Build> query(@Nullable Project project, EntityQuery<Build> buildQuery, 
			int firstResult, int maxResults) {
		CriteriaQuery<Build> criteriaQuery = buildCriteriaQuery(project, getSession(), buildQuery);
		Query<Build> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	private void applyOrders(Root<Build> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder, 
			EntityQuery<Build> buildQuery) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: buildQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(BuildQuery.getPath(root, Build.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(BuildQuery.getPath(root, Build.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get(Build.PROP_SUBMIT_DATE)));
		criteriaQuery.orderBy(orders);
	}
	
	@Sessional
	@Override
	public Collection<Long> queryIds(Project project, EntityQuery<Build> buildQuery, 
			int firstResult, int maxResults) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Build> root = criteriaQuery.from(Build.class);
		criteriaQuery.select(root.get(Build.PROP_ID));

		criteriaQuery.where(getPredicates(project, buildQuery.getCriteria(), root, builder));

		applyOrders(root, criteriaQuery, builder, buildQuery);

		Query<Long> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		
		return query.list();
	}
	
	@Sessional
	@Override
	public int count(@Nullable Project project, io.onedev.server.search.entity.EntityCriteria<Build> buildCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Build> root = criteriaQuery.from(Build.class);

		criteriaQuery.where(getPredicates(project, buildCriteria, root, builder));

		criteriaQuery.select(builder.count(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
	}
	
	@Sessional
	@Override
	public Map<ObjectId, Map<String, Collection<StatusInfo>>> queryStatus(Project project, Collection<ObjectId> commitIds) {
		Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatuses = new HashMap<>();
		
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
		for (ObjectId commitId: commitIds) {
			if (!commitStatuses.containsKey(commitId))
				commitStatuses.put(commitId, new HashMap<>());
		}
		return commitStatuses;
	}
	
	@SuppressWarnings("unchecked")
	private void fillStatus(Project project, Collection<ObjectId> commitIds, 
			Map<ObjectId, Map<String, Collection<StatusInfo>>> commitStatuses) {
		Query<?> query = getSession().createQuery("select commitHash, jobName, status, refName, request.id from Build "
				+ "where project=:project and commitHash in :commitHashes");
		query.setParameter("project", project);
		query.setParameter("commitHashes", commitIds.stream().map(it->it.name()).collect(Collectors.toList()));
		for (Object[] row: (List<Object[]>)query.list()) {
			ObjectId commitId = ObjectId.fromString((String) row[0]);
			String jobName = (String) row[1];
			Status status = (Status) row[2];
			String refName = (String) row[3];
			Long requestId = (Long) row[4];
			
			Map<String, Collection<StatusInfo>> commitStatus = commitStatuses.get(commitId);
			if (commitStatus == null) {
				commitStatus = new HashMap<>();
				commitStatuses.put(commitId, commitStatus);
			}
			Collection<StatusInfo> jobStatus = commitStatus.get(jobName);
			if (jobStatus == null) {
				jobStatus = new HashSet<>();
				commitStatus.put(jobName, jobStatus);
			}
			jobStatus.add(new StatusInfo(status, requestId, refName));
		}
	}
	
	@Sessional
	protected long getMaxId() {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> query = builder.createQuery(Long.class);
		Root<Build> root = query.from(Build.class);
		query.select(builder.max(root.get(Build.PROP_ID)));
		Long maxId = getSession().createQuery(query).getSingleResult();
		return maxId!=null?maxId:0;
	}

	@Override
	public void execute() {
		long maxId = getMaxId();
		Collection<Long> idsToPreserve = new HashSet<>();
		
		sessionManager.run(new Runnable() {

			@Override
			public void run() {
				for (Project project: projectManager.query()) {
					logger.debug("Populating preserved build ids of project '" + project.getName() + "'...");
					List<BuildPreservation> preservations = project.getBuildSetting().getBuildPreservations();
					if (preservations.isEmpty()) {
						idsToPreserve.addAll(queryIds(project, new BuildQuery(), 0, Integer.MAX_VALUE));
					} else {
						for (BuildPreservation preservation: preservations) {
							try {
								BuildQuery query = BuildQuery.parse(project, preservation.getCondition(), false, false);
								int count;
								if (preservation.getCount() != null)
									count = preservation.getCount();
								else
									count = Integer.MAX_VALUE;
								idsToPreserve.addAll(queryIds(project, query, 0, count));
							} catch (Exception e) {
								String message = String.format("Error parsing build preserve condition(project: %s, condition: %s)", 
										project.getName(), preservation.getCondition());
								logger.error(message, e);
								idsToPreserve.addAll(queryIds(project, new BuildQuery(), 0, Integer.MAX_VALUE));
							}
						}
					}
				}
			}
			
		});

		EntityCriteria<Build> criteria = newCriteria();
		AtomicInteger firstResult = new AtomicInteger(0);
		
		while (transactionManager.call(new Callable<Boolean>() {

			@Override
			public Boolean call() {
				List<Build> builds = query(criteria, firstResult.get(), CLEANUP_BATCH);
				if (!builds.isEmpty()) {
					logger.debug("Checking build preservation: {}->{}", 
							firstResult.get()+1, firstResult.get()+builds.size());
				}
				for (Build build: builds) {
					if (build.isFinished() && build.getId() <= maxId && !idsToPreserve.contains(build.getId())) {
						logger.debug("Deleting build " + build.getFQN() + "...");
						delete(build);
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
	
	@SuppressWarnings("unchecked")
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching build info...");
		
		Query<?> query = dao.getSession().createQuery("select id, project.id, commitHash, jobName from Build");
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long buildId = (Long) fields[0];
			Long projectId = (Long)fields[1];
			builds.put(buildId, new BuildFacade(buildId, projectId, (String)fields[2]));
			populateJobNames(projectId, (String)fields[3]);
		}
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}

	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof Build) {
			Build build = (Build) event.getEntity();
			Long projectId = build.getProject().getId();
			String jobName = build.getJobName();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					jobNamesLock.writeLock().lock();
					try {
						populateJobNames(projectId, jobName);
					} finally {
						jobNamesLock.writeLock().unlock();
					}
				}
				
			});
		}
	}
	
	private CriteriaQuery<Object[]> buildStreamPreviousQuery(Build build, Status status, String...fields) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> query = builder.createQuery(Object[].class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("project"), build.getProject()));
		predicates.add(builder.equal(root.get("jobName"), build.getJobName()));
		if (status != null)
			predicates.add(builder.equal(root.get("status"), status));
		predicates.add(builder.lessThan(root.get("number"), build.getNumber()));
		query.where(predicates.toArray(new Predicate[0]));
		List<Selection<?>> selections = new ArrayList<>();
		for (String field: fields)
			selections.add(root.get(field));
		query.multiselect(selections);
		
		return query;
	}
	
	@Sessional
	@Override
	public Collection<Long> queryStreamPreviousNumbers(Build build, Status status, int limit) {
		Map<ObjectId, Long> buildNumbers = new HashMap<>();
		for (Object[] fields: getSession().createQuery(buildStreamPreviousQuery(build, status, "commitHash", "number")).list()) {
			buildNumbers.put(ObjectId.fromString((String) fields[0]), (Long)fields[1]);
		}
		
		Collection<Long> prevBuildNumbers = new HashSet<>();
		if (!buildNumbers.isEmpty()) {
			try (RevWalk revWalk = new RevWalk(build.getProject().getRepository())) {
				revWalk.markStart(revWalk.lookupCommit(build.getCommitId()));
				RevCommit nextCommit;
				while ((nextCommit = revWalk.next()) != null) {
					Long buildNumber = buildNumbers.remove(nextCommit);
					if (buildNumber != null) {
						prevBuildNumbers.add(buildNumber);
						if (prevBuildNumbers.size() >= limit || buildNumbers.isEmpty())
							break;
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return prevBuildNumbers;
	}

	@Sessional
	@Override
	public Build findStreamPrevious(Build build, Status status) {
		Map<ObjectId, Long> buildIds = new HashMap<>();
		for (Object[] fields: getSession().createQuery(buildStreamPreviousQuery(
				build, status, "commitHash", "id")).list()) {
			buildIds.put(ObjectId.fromString((String) fields[0]), (Long)fields[1]);
		}
		
		if (!buildIds.isEmpty()) {
			try (RevWalk revWalk = new RevWalk(build.getProject().getRepository())) {
				revWalk.markStart(revWalk.lookupCommit(build.getCommitId()));
				RevCommit nextCommit;
				while ((nextCommit = revWalk.next()) != null) {
					Long buildId = buildIds.get(nextCommit);
					if (buildId != null)
						return load(buildId);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	@Override
	public Collection<Long> getIdsByProject(Long projectId) {
		buildsLock.readLock().lock();
		try {
			Collection<Long> buildIds = new HashSet<>();
			for (BuildFacade build: builds.values()) {
				if (build.getProjectId().equals(projectId))
					buildIds.add(build.getId());
			}
			return buildIds;
		} finally {
			buildsLock.readLock().unlock();
		}
	}

	@Override
	public Collection<Long> filterIds(Long projectId, Collection<String> commitHashes) {
		buildsLock.readLock().lock();
		try {
			Collection<Long> buildIds = new HashSet<>();
			for (BuildFacade build: builds.values()) {
				if (build.getProjectId().equals(projectId) 
						&& commitHashes.contains(build.getCommitHash())) {
					buildIds.add(build.getId());
				}
			}
			return buildIds;
		} finally {
			buildsLock.readLock().unlock();
		}
	}
	
	private void populateJobNames(Long projectId, String jobName) {
		Collection<String> jobNamesOfProject = jobNames.get(projectId);
		if (jobNamesOfProject == null) {
			jobNamesOfProject = new HashSet<>();
			jobNames.put(projectId, jobNamesOfProject);
		}
		jobNamesOfProject.add(jobName);
	}

	@Override
	public Collection<String> getJobNames(@Nullable Project project) {
		jobNamesLock.readLock().lock();
		try {
			Collection<String> jobNames = new HashSet<>();
			for (Map.Entry<Long, Collection<String>> entry: this.jobNames.entrySet()) { 
				if (project == null || project.getId().equals(entry.getKey()))
					jobNames.addAll(entry.getValue());
			}
			return jobNames;
		} finally {
			jobNamesLock.readLock().unlock();
		}
	}
	
	private void populateAccessibleJobNames(Map<Project, Collection<String>> accessibleJobNames, 
			Map<Long, Collection<String>> availableJobNames, Project project, Role role) {
		Collection<String> availableJobNamesOfProject = availableJobNames.get(project.getId());
		if (availableJobNamesOfProject != null) {
			for (String jobName: availableJobNamesOfProject) {
				if (role.implies(new JobPermission(jobName, new AccessBuild()))) {
					Collection<String> accessibleJobNamesOfProject = accessibleJobNames.get(project);
					if (accessibleJobNamesOfProject == null) {
						accessibleJobNamesOfProject = new HashSet<>();
						accessibleJobNames.put(project, accessibleJobNamesOfProject);
					}
					accessibleJobNamesOfProject.add(jobName);
				}
			}
		}
	}
	
	private void populateAccessibleJobNames(Collection<String> accessibleJobNames, 
			Collection<String> availableJobNames, Role role) {
		for (String jobName: availableJobNames) {
			if (role.implies(new JobPermission(jobName, new AccessBuild())))
				accessibleJobNames.add(jobName);
		}
	}

	@Override
	public Map<Project, Collection<String>> getAccessibleJobNames() {
		jobNamesLock.readLock().lock();
		try {
			Map<Project, Collection<String>> accessibleJobNames = new HashMap<>();
			User user = SecurityUtils.getUser();
			
			if (SecurityUtils.isAdministrator()) {
				for (Map.Entry<Long, Collection<String>> entry: jobNames.entrySet())
					accessibleJobNames.put(projectManager.load(entry.getKey()), new HashSet<>(entry.getValue()));
			} else {
				if (user != null) {
					for (UserAuthorization authorization: user.getAuthorizations()) {
						populateAccessibleJobNames(accessibleJobNames, jobNames, 
								authorization.getProject(), authorization.getRole());
					}
					for (Group group: user.getGroups()) {
						for (GroupAuthorization authorization: group.getAuthorizations()) {
							populateAccessibleJobNames(accessibleJobNames, jobNames, 
									authorization.getProject(), authorization.getRole());
						}
					}
				}
				Group group = groupManager.findAnonymous();
				if (group != null) {
					for (GroupAuthorization authorization: group.getAuthorizations()) {
						populateAccessibleJobNames(accessibleJobNames, jobNames, 
								authorization.getProject(), authorization.getRole());
					}
				}
			}
			return accessibleJobNames;
		} finally {
			jobNamesLock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<String> getAccessibleJobNames(Project project) {
		jobNamesLock.readLock().lock();
		try {
			Collection<String> accessibleJobNames = new HashSet<>();
			Collection<String> availableJobNames = jobNames.get(project.getId());
			if (availableJobNames != null) {
				if (SecurityUtils.isAdministrator()) {
					accessibleJobNames.addAll(availableJobNames);
				} else {
					User user = SecurityUtils.getUser();
					if (user != null) {
						for (UserAuthorization authorization: user.getAuthorizations()) {
							if (project.equals(authorization.getProject())) {
								populateAccessibleJobNames(accessibleJobNames, availableJobNames, 
										authorization.getRole());
							}
						}
						for (Group group: user.getGroups()) {
							for (GroupAuthorization authorization: group.getAuthorizations()) {
								if (project.equals(authorization.getProject())) {
									populateAccessibleJobNames(accessibleJobNames, availableJobNames, 
											authorization.getRole());
								}
							}
						}
					}
					Group group = groupManager.findAnonymous();
					if (group != null) {
						for (GroupAuthorization authorization: group.getAuthorizations()) {
							if (project == null || project.equals(authorization.getProject())) {
								populateAccessibleJobNames(accessibleJobNames, availableJobNames, 
										authorization.getRole());
							}
						}
					}
				}
			}
			return accessibleJobNames;
		} finally {
			jobNamesLock.readLock().unlock();
		}
	}

	@Override
	public void populateBuilds(Collection<PullRequest> requests) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		
		Root<Build> root = query.from(Build.class);
		query.select(root);
		root.join(Build.PROP_PULL_REQUEST);
		
		query.where(root.get(Build.PROP_PULL_REQUEST).in(requests));
		
		for (PullRequest request: requests)
			request.setBuilds(new ArrayList<>());
		
		for (Build build: getSession().createQuery(query).getResultList())
			build.getRequest().getBuilds().add(build);
	}
	
}
