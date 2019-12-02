package io.onedev.server.entitymanager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.errors.MissingObjectException;
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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.match.StringMatcher;
import io.onedev.commons.utils.schedule.SchedulableTask;
import io.onedev.commons.utils.schedule.TaskScheduler;
import io.onedev.server.OneException;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.GroupManager;
import io.onedev.server.entitymanager.ProjectManager;
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
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.role.JobPrivilege;
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
import io.onedev.server.security.permission.ManageProject;
import io.onedev.server.security.permission.ProjectPermission;
import io.onedev.server.security.permission.SystemAdministration;
import io.onedev.server.storage.StorageManager;
import io.onedev.server.util.BuildConstants;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.patternset.PatternSet;

@Singleton
public class DefaultBuildManager extends AbstractEntityManager<Build> implements BuildManager, SchedulableTask {

	private static final int STATUS_QUERY_BATCH = 500;
	
	private static final int CLEANUP_BATCH = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildManager.class);
	
	private final BuildParamManager buildParamManager;
	
	private final BuildDependenceManager buildDependenceManager;
	
	private final StorageManager storageManager;
	
	private final GroupManager groupManager;
	
	private final ProjectManager projectManager;
	
	private final TaskScheduler taskScheduler;
	
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
			ProjectManager projectManager, TransactionManager transactionManager) {
		super(dao);
		this.buildParamManager = buildParamManager;
		this.buildDependenceManager = buildDependenceManager;
		this.storageManager = storageManager;
		this.groupManager = groupManager;
		this.projectManager = projectManager;
		this.taskScheduler = taskScheduler;
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
		criteria.add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("number", number));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Build find(String buildFQN) {
		String projectName = StringUtils.substringBefore(buildFQN, "#");
		Project project = projectManager.find(projectName);
		if (project != null) {
			String buildNumberStr = StringUtils.substringAfter(buildFQN, "#");
			try {
				Long buildNumber = Long.valueOf(buildNumberStr);
				return find(project, buildNumber);
			} catch (NumberFormatException e) {
				throw new OneException("Invalid build number: " + buildNumberStr);
			}
		} else {
			return null;
		}
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
		return query(project, commitId, jobName, new HashMap<>());
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName, Map<String, List<String>> params) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get("project"), project));
		predicates.add(builder.equal(root.get("commitHash"), commitId.name()));
		if (jobName != null)
			predicates.add(builder.equal(root.get("jobName"), jobName));
		
		for (Map.Entry<String, List<String>> entry: params.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				for (String value: entry.getValue()) {
					Join<?, ?> join = root.join(BuildConstants.ATTR_PARAMS, JoinType.INNER);
					predicates.add(builder.equal(join.get(BuildParam.ATTR_NAME), entry.getKey()));
					predicates.add(builder.equal(join.get(BuildParam.ATTR_VALUE), value));
				}
			} else {
				Join<?, ?> join = root.join(BuildConstants.ATTR_PARAMS, JoinType.INNER);
				predicates.add(builder.equal(join.get(BuildParam.ATTR_NAME), entry.getKey()));
				predicates.add(builder.isNull(join.get(BuildParam.ATTR_VALUE)));
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
				Restrictions.eq("status", Status.PENDING), 
				Restrictions.eq("status", Status.RUNNING), 
				Restrictions.eq("status", Status.WAITING)));
		criteria.setCacheable(true);
		return query(criteria);
	}

	@Sessional
	@Override
	public List<Build> query(Project project, @Nullable User user, String term, int count) {
		List<Build> builds = new ArrayList<>();

		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq(BuildConstants.ATTR_PROJECT, project));

		Subject subject = User.asSubject(user);
		if (!subject.isPermitted(new ProjectPermission(project, new ManageProject()))) {
			List<Criterion> jobCriterions = new ArrayList<>();
			for (String jobName: getAccessibleJobNames(project, user).get(project)) 
				jobCriterions.add(Restrictions.eq(BuildConstants.ATTR_JOB, jobName));
			if (!jobCriterions.isEmpty())
				criteria.add(Restrictions.or(jobCriterions.toArray(new Criterion[jobCriterions.size()])));
			else
				return builds;
		}

		if (term.startsWith("#"))
			term = term.substring(1);
		if (term.length() != 0) {
			try {
				long buildNumber = Long.parseLong(term);
				criteria.add(Restrictions.eq("number", buildNumber));
			} catch (NumberFormatException e) {
				criteria.add(Restrictions.or(
						Restrictions.ilike("version", term, MatchMode.ANYWHERE),
						Restrictions.ilike("jobName", term, MatchMode.ANYWHERE)));
			}
		}
		
		criteria.addOrder(Order.desc("number"));
		builds.addAll(query(criteria, 0, count));
		
		return builds;
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

	private Predicate[] getPredicates(io.onedev.server.search.entity.EntityCriteria<Build> criteria, 
			@Nullable Project project, Root<Build> root, CriteriaBuilder builder, @Nullable User user) {
		List<Predicate> predicates = new ArrayList<>();

		Subject subject = User.asSubject(user);
		if (project != null) {
			predicates.add(builder.equal(root.get(BuildConstants.ATTR_PROJECT), project));
			if (!subject.isPermitted(new ProjectPermission(project, new ManageProject()))) {
				List<Predicate> jobPredicates = new ArrayList<>();
				for (String jobName: getAccessibleJobNames(project, user).get(project)) 
					jobPredicates.add(builder.equal(root.get(BuildConstants.ATTR_JOB), jobName));
				predicates.add(builder.or(jobPredicates.toArray(new Predicate[jobPredicates.size()])));
			}
		} else if (!subject.isPermitted(new SystemAdministration())) {
			List<Predicate> projectPredicates = new ArrayList<>();
			for (Map.Entry<Project, Collection<String>> entry: getAccessibleJobNames(null, user).entrySet()) {
				if (subject.isPermitted(new ProjectPermission(project, new ManageProject()))) {
					projectPredicates.add(builder.equal(root.get(BuildConstants.ATTR_PROJECT), entry.getKey()));
				} else {
					List<Predicate> jobPredicates = new ArrayList<>();
					for (String jobName: entry.getValue()) 
						jobPredicates.add(builder.equal(root.get(BuildConstants.ATTR_JOB), jobName));
					projectPredicates.add(builder.and(
							builder.equal(root.get(BuildConstants.ATTR_PROJECT), entry.getKey()), 
							builder.or(jobPredicates.toArray(new Predicate[jobPredicates.size()]))));
				}
			}
			predicates.add(builder.or(projectPredicates.toArray(new Predicate[projectPredicates.size()])));
		}
		
		if (criteria != null)
			predicates.add(criteria.getPredicate(root, builder, user));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Build> buildCriteriaQuery(Session session, @Nullable Project project, 
			EntityQuery<Build> buildQuery, @Nullable User user) {
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
			orders.add(builder.desc(root.get(BuildConstants.ATTR_ID)));
		query.orderBy(orders);
		
		return query;
	}
	
	@Sessional
	@Override
	public List<Build> query(@Nullable Project project, @Nullable User user, 
			EntityQuery<Build> buildQuery, int firstResult, int maxResults) {
		CriteriaQuery<Build> criteriaQuery = buildCriteriaQuery(getSession(), project, buildQuery, user);
		Query<Build> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		return query.getResultList();
	}

	@Sessional
	@Override
	public int count(@Nullable Project project, @Nullable User user, 
			io.onedev.server.search.entity.EntityCriteria<Build> buildCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Build> root = criteriaQuery.from(Build.class);

		criteriaQuery.where(getPredicates(buildCriteria, project, root, builder, user));

		criteriaQuery.select(builder.countDistinct(root));
		return getSession().createQuery(criteriaQuery).uniqueResult().intValue();
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
							query = Optional.of(BuildQuery.parse(project, queryString));
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

	private CriteriaQuery<Object[]> buildQueryOfStreamPrevios(Build build, Status status, String...fields) {
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
	public Collection<Long> queryNumbersOfStreamPrevious(Build build, Status status, int limit) {
		Map<ObjectId, Long> buildNumbers = new HashMap<>();
		for (Object[] fields: getSession().createQuery(buildQueryOfStreamPrevios(build, status, "commitHash", "number")).list()) {
			buildNumbers.put(ObjectId.fromString((String) fields[0]), (Long)fields[1]);
		}
		
		Collection<Long> prevBuildNumbers = new HashSet<>();
		try (RevWalk revWalk = new RevWalk(build.getProject().getRepository())) {
			RevCommit current = revWalk.lookupCommit(build.getCommitId());
			revWalk.parseHeaders(current);
			while (current.getParentCount() != 0) {
				RevCommit firstParent = current.getParent(0);
				Long buildNumber = buildNumbers.get(firstParent);
				if (buildNumber != null) {
					prevBuildNumbers.add(buildNumber);
					if (prevBuildNumbers.size() >= limit)
						break;
				}
				current = firstParent;
				revWalk.parseHeaders(current);
			} 
		} catch (MissingObjectException e) {
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return prevBuildNumbers;
	}

	@Sessional
	@Override
	public Build findStreamPrevious(Build build, Status status) {
		Map<ObjectId, Long> buildIds = new HashMap<>();
		for (Object[] fields: getSession().createQuery(buildQueryOfStreamPrevios(build, status, "commitHash", "id")).list()) {
			buildIds.put(ObjectId.fromString((String) fields[0]), (Long)fields[1]);
		}
		
		try (RevWalk revWalk = new RevWalk(build.getProject().getRepository())) {
			RevCommit current = revWalk.lookupCommit(build.getCommitId());
			revWalk.parseHeaders(current);
			while (current.getParentCount() != 0) {
				RevCommit firstParent = current.getParent(0);
				Long buildId = buildIds.get(firstParent);
				if (buildId != null)
					return load(buildId);
				current = firstParent;
				revWalk.parseHeaders(current);
			} 
		} catch (MissingObjectException e) {
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	@Override
	public Collection<Long> getBuildIdsByProject(Long projectId) {
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
	public Collection<Long> filterBuildIds(Long projectId, Collection<String> commitHashes) {
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

	private Collection<String> getAccessibleJobNames(Role role, Collection<String> jobNames) {
		Collection<String> accessibleJobNames = new HashSet<>();
		if (role.isManageProject()) {
			accessibleJobNames.addAll(jobNames);
		} else {
			StringMatcher matcher = new StringMatcher();
			for (JobPrivilege jobPrivilege: role.getJobPrivileges()) {
				PatternSet patternSet = PatternSet.fromString(jobPrivilege.getJobNames());
				for (String jobName: jobNames) {
					if (patternSet.matches(matcher, jobName))
						accessibleJobNames.add(jobName);
				}
			}
		}
		return accessibleJobNames;
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
			Map<Long, Collection<String>> jobNames, Project project, Role role) {
		Collection<String> jobNamesOfProject = jobNames.get(project.getId());
		if (jobNamesOfProject != null) {
			Collection<String> accessibleJobNamesOfProject = getAccessibleJobNames(role, jobNamesOfProject);
			Collection<String> currentAccessibleJobNamesOfProject = accessibleJobNames.get(project);
			if (currentAccessibleJobNamesOfProject == null) {
				currentAccessibleJobNamesOfProject = new HashSet<>();
				accessibleJobNames.put(project, currentAccessibleJobNamesOfProject);
			}
			currentAccessibleJobNamesOfProject.addAll(accessibleJobNamesOfProject);
		}
	}
	
	private Map<Project, Collection<String>> getAccessibleJobNames(@Nullable Project project, @Nullable User user) {
		jobNamesLock.readLock().lock();
		try {
			Map<Project, Collection<String>> accessibleJobNames = new HashMap<>();
			Subject subject = User.asSubject(user);
			if (subject.isPermitted(new SystemAdministration())) {
				for (Map.Entry<Long, Collection<String>> entry: jobNames.entrySet())
					accessibleJobNames.put(projectManager.load(entry.getKey()), new HashSet<>(entry.getValue()));
			} else {
				if (user != null) {
					for (UserAuthorization authorization: user.getProjectAuthorizations()) {
						if (project == null || project.equals(authorization.getProject())) {
							populateAccessibleJobNames(accessibleJobNames, jobNames, 
									authorization.getProject(), authorization.getRole());
						}
					}
					for (Group group: user.getGroups()) {
						for (GroupAuthorization authorization: group.getProjectAuthorizations()) {
							if (project == null || project.equals(authorization.getProject())) {
								populateAccessibleJobNames(accessibleJobNames, jobNames, 
										authorization.getProject(), authorization.getRole());
							}
						}
					}
				}
				Group group = groupManager.findAnonymous();
				if (group != null) {
					for (GroupAuthorization authorization: group.getProjectAuthorizations()) {
						if (project == null || project.equals(authorization.getProject())) {
							populateAccessibleJobNames(accessibleJobNames, jobNames, 
									authorization.getProject(), authorization.getRole());
						}
					}
				}
			}
			if (project != null && !accessibleJobNames.containsKey(project))
				accessibleJobNames.put(project, new HashSet<>());
			return accessibleJobNames;
		} finally {
			jobNamesLock.readLock().unlock();
		}
	}

}
