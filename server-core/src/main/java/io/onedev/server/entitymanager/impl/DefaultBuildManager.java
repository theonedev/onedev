package io.onedev.server.entitymanager.impl;

import java.io.File;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
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
import com.hazelcast.core.HazelcastInstance;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.BuildDependenceManager;
import io.onedev.server.entitymanager.BuildManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.Agent;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.model.BuildDependence;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.build.BuildPreservation;
import io.onedev.server.persistence.SequenceGenerator;
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
import io.onedev.server.util.FileInfo;
import io.onedev.server.util.MimeFileInfo;
import io.onedev.server.util.ProjectBuildStats;
import io.onedev.server.util.ProjectScopedNumber;
import io.onedev.server.util.StatusInfo;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.facade.BuildFacade;
import io.onedev.server.util.schedule.SchedulableTask;
import io.onedev.server.util.schedule.TaskScheduler;

@Singleton
public class DefaultBuildManager extends BaseEntityManager<Build> implements BuildManager, SchedulableTask, Serializable {

	private static final int STATUS_QUERY_BATCH = 500;
	
	private static final int CLEANUP_BATCH = 5000;
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildManager.class);
	
	private final BuildParamManager buildParamManager;
	
	private final BuildDependenceManager buildDependenceManager;
	
	private final ProjectManager projectManager;
	
	private final TaskScheduler taskScheduler;
	
	private final SessionManager sessionManager;
	
	private final TransactionManager transactionManager;
	
	private final SettingManager settingManager;
	
	private final ClusterManager clusterManager;
	
	private final SequenceGenerator numberGenerator;
	
	private volatile Map<Long, BuildFacade> cache;
	
	private volatile Map<Long, Collection<String>> jobNames;
	
	private volatile String taskId;
	
	@Inject
	public DefaultBuildManager(Dao dao, BuildParamManager buildParamManager, 
			TaskScheduler taskScheduler, BuildDependenceManager buildDependenceManager,
			ProjectManager projectManager, SessionManager sessionManager, 
			TransactionManager transactionManager, SettingManager settingManager, 
			ClusterManager clusterManager) {
		super(dao);
		this.buildParamManager = buildParamManager;
		this.buildDependenceManager = buildDependenceManager;
		this.projectManager = projectManager;
		this.taskScheduler = taskScheduler;
		this.sessionManager = sessionManager;
		this.transactionManager = transactionManager;
		this.settingManager = settingManager;
		this.clusterManager = clusterManager;
		
		numberGenerator = new SequenceGenerator(Build.class, clusterManager, dao);
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(BuildManager.class);
	}
	
	@Transactional
	@Override
	public void delete(Build build) {
    	super.delete(build);
    	
		Long buildId = build.getId();
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				cache.remove(buildId);
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
	public Build findLastFinished(Project project, String jobName, @Nullable String refName) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq(Build.PROP_PROJECT, project));
		criteria.add(Restrictions.eq(Build.PROP_JOB, jobName));
		if (refName != null) {
			refName = refName.replace("*", "%");
			criteria.add(Restrictions.ilike(Build.PROP_REF_NAME, refName));
		}
		criteria.add(Build.Status.ofFinished());
		criteria.addOrder(Order.desc(Build.PROP_NUMBER));
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
				cache.put(facade.getId(), facade);
				populateJobNames(facade.getProjectId(), jobName);
			}
			
		});
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Project project = (Project) event.getEntity();
	    	if (project.getForkRoot().equals(project))
	    		numberGenerator.removeNextSequence(project);
			
			Long projectId = project.getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					for (var id: cache.entrySet().stream()
							.filter(it->it.getValue().getProjectId().equals(projectId))
							.map(it->it.getKey())
							.collect(Collectors.toSet())) {
						cache.remove(id);
					}
					jobNames.remove(projectId);
				}
			});
		}
	}

	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String pipeline) {
		return query(project, commitId, null, pipeline);
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName, String pipeline) {
		return query(project, commitId, jobName, null, null, new HashMap<>(), pipeline);
	}
	
	@Sessional
	@Override
	public Collection<Build> query(Project project, ObjectId commitId, String jobName, 
			String refName, Optional<PullRequest> request, Map<String, List<String>> params, 
			String pipeline) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(root.get(Build.PROP_PROJECT), project));
		predicates.add(builder.equal(root.get(Build.PROP_COMMIT), commitId.name()));
		if (pipeline != null)
			predicates.add(builder.equal(root.get(Build.PROP_PIPELINE), pipeline));
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
			
		predicates.addAll(getPredicates(root, builder, params));
		
		query.where(predicates.toArray(new Predicate[0]));
		return getSession().createQuery(query).list();
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Map<Long, Long> queryUnfinished() {
		Query<?> query = getSession().createQuery("select id, project.id from Build where "
				+ "status=:waiting or status=:pending or status=:running");
		query.setParameter("waiting", Build.Status.WAITING);
		query.setParameter("pending", Build.Status.PENDING);
		query.setParameter("running", Build.Status.RUNNING);
		
		Map<Long, Long> result = new HashMap<>();
		for (Object[] fields: (List<Object[]>)query.list()) 
			result.put((Long)fields[0], (Long)fields[1]);
		return result;
	}

	@Sessional
	@Override
	public Collection<Build> queryUnfinished(Project project, String jobName, @Nullable String refName, 
			@Nullable Optional<PullRequest> request, Map<String, List<String>> params) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		
		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.or(
				builder.equal(root.get(Build.PROP_STATUS), Build.Status.PENDING),
				builder.equal(root.get(Build.PROP_STATUS), Build.Status.RUNNING),
				builder.equal(root.get(Build.PROP_STATUS), Build.Status.WAITING)));
		predicates.add(builder.equal(root.get(Build.PROP_PROJECT), project));
		predicates.add(builder.equal(root.get(Build.PROP_JOB), jobName));
		
		if (refName != null)
			predicates.add(builder.equal(root.get(Build.PROP_REF_NAME), refName));

		if (request != null) {
			if (request.isPresent())
				predicates.add(builder.equal(root.get(Build.PROP_PULL_REQUEST), request.get()));
			else
				predicates.add(builder.isNull(root.get(Build.PROP_PULL_REQUEST)));
		}
			
		predicates.addAll(getPredicates(root, builder, params));
		
		query.where(predicates.toArray(new Predicate[0]));
		return getSession().createQuery(query).list();
	}
	
	private List<Predicate> getPredicates(From<Build, Build> root, CriteriaBuilder builder, 
			Map<String, List<String>> params) {
		List<Predicate> predicates = new ArrayList<>();
		
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
		
		return predicates;
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, String term, int count) {
		List<Build> builds = new ArrayList<>();

		EntityCriteria<Build> criteria = newCriteria();

		if (term.contains("#")) {
			String projectPath = StringUtils.substringBefore(term, "#");
			Project specifiedProject = projectManager.findByPath(projectPath);
			if (specifiedProject != null && SecurityUtils.canAccess(specifiedProject)) {
				project = specifiedProject;
				term = StringUtils.substringAfter(term, "#");
			}
		}
		
		Set<Project> projects = Sets.newHashSet(project);
		projects.addAll(project.getForkParents());

		List<Criterion> projectCriterions = new ArrayList<>();
		for (Project each: projects) {
			Collection<String> availableJobNames = jobNames.get(each.getId());
			if (availableJobNames != null && !availableJobNames.isEmpty()) {
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
		build.setNumber(numberGenerator.getNextSequence(build.getNumberScope()));
		save(build);
		for (BuildParam param: build.getParams())
			buildParamManager.save(param);
		for (BuildDependence dependence: build.getDependencies())
			buildDependenceManager.save(dependence);
	}

	private Collection<Predicate> getPredicates(@Nullable Project project, From<Build, Build> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = new ArrayList<>();

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
		
		return predicates;
	}
	
	private Predicate[] getPredicates(@Nullable Project project, @Nullable Criteria<Build> criteria, 
			CriteriaQuery<?> query, From<Build, Build> root, CriteriaBuilder builder) {
		Collection<Predicate> predicates = getPredicates(project, root, builder);
		if (criteria != null) 
			predicates.add(criteria.getPredicate(query, root, builder));
		return predicates.toArray(new Predicate[0]);
	}
	
	private CriteriaQuery<Build> buildCriteriaQuery(@Nullable Project project, 
			Session session,  EntityQuery<Build> buildQuery) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Build> query = builder.createQuery(Build.class);
		Root<Build> root = query.from(Build.class);
		query.select(root);
		
		query.where(getPredicates(project, buildQuery.getCriteria(), query, root, builder));

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

	private void applyOrders(From<Build, Build> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder builder, 
			EntityQuery<Build> buildQuery) {
		List<javax.persistence.criteria.Order> orders = new ArrayList<>();
		for (EntitySort sort: buildQuery.getSorts()) {
			if (sort.getDirection() == Direction.ASCENDING)
				orders.add(builder.asc(BuildQuery.getPath(root, Build.ORDER_FIELDS.get(sort.getField()))));
			else
				orders.add(builder.desc(BuildQuery.getPath(root, Build.ORDER_FIELDS.get(sort.getField()))));
		}

		if (orders.isEmpty())
			orders.add(builder.desc(root.get(Build.PROP_NUMBER)));
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

		criteriaQuery.where(getPredicates(project, buildQuery.getCriteria(), criteriaQuery, root, builder));

		applyOrders(root, criteriaQuery, builder, buildQuery);

		Query<Long> query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResults);
		
		return query.list();
	}
	
	@Sessional
	@Override
	public int count(@Nullable Project project, Criteria<Build> buildCriteria) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
		Root<Build> root = criteriaQuery.from(Build.class);

		criteriaQuery.where(getPredicates(project, buildCriteria, criteriaQuery, root, builder));

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
		Query<?> query = getSession().createQuery("select commitHash, pipeline, jobName, status, refName, request.id from Build "
				+ "where project=:project and commitHash in :commitHashes");
		query.setParameter("project", project);
		query.setParameter("commitHashes", commitIds.stream().map(it->it.name()).collect(Collectors.toList()));
		for (Object[] row: (List<Object[]>)query.list()) {
			ObjectId commitId = ObjectId.fromString((String) row[0]);
			String pipeline = (String) row[1];
			String jobName = (String) row[2];
			Status status = (Status) row[3];
			String refName = (String) row[4];
			Long requestId = (Long) row[5];
			
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
			jobStatus.add(new StatusInfo(status, pipeline, requestId, refName));
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
		if (clusterManager.isLeaderServer()) {
			long maxId = getMaxId();
			Collection<Long> idsToPreserve = new HashSet<>();
			
			sessionManager.run(new Runnable() {

				@Override
				public void run() {
					for (Project project: projectManager.query()) {
						logger.debug("Populating preserved build ids of project '" + project.getPath() + "'...");
						List<BuildPreservation> preservations = project.getHierarchyBuildPreservations();
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
											project.getPath(), preservation.getCondition());
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
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.dailyAtHourAndMinute(0, 0);
	}
	
	@SuppressWarnings("unchecked")
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching build info...");
		
		HazelcastInstance hazelcastInstance = clusterManager.getHazelcastInstance();
        cache = hazelcastInstance.getReplicatedMap("buildCache");
        jobNames = hazelcastInstance.getReplicatedMap("jobNames");
        
		Query<?> query = dao.getSession().createQuery("select id, project.id, number, commitHash, jobName from Build");
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long buildId = (Long) fields[0];
			Long projectId = (Long)fields[1];
			Long buildNumber = (Long) fields[2];
			cache.put(buildId, new BuildFacade(buildId, projectId, buildNumber, (String)fields[3]));
			populateJobNames(projectId, (String)fields[4]);
		}
		
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		if (taskId != null)
			taskScheduler.unschedule(taskId);
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
		CriteriaQuery<Object[]> query = buildStreamPreviousQuery(build, status, "commitHash", "number");
		for (Object[] fields: getSession().createQuery(query).list()) {
			buildNumbers.put(ObjectId.fromString((String) fields[0]), (Long)fields[1]);
		}
		
		if (!buildNumbers.isEmpty()) {
			return getGitService().filterParents(build.getProject(), build.getCommitId(), 
					buildNumbers, limit);
		} else { 
			return new HashSet<>();
		}
	}
	
	private GitService getGitService() {
		return OneDev.getInstance(GitService.class);
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
			Collection<Long> filtered = getGitService().filterParents(
					build.getProject(), build.getCommitId(), buildIds, 1);
			if (!filtered.isEmpty())
				return load(filtered.iterator().next());
		}
		return null;
	}
	
	@Override
	public Collection<Long> getNumbersByProject(Long projectId) {
		Collection<Long> buildNumbers = new HashSet<>();
		for (BuildFacade build: cache.values()) {
			if (build.getProjectId().equals(projectId))
				buildNumbers.add(build.getNumber());
		}
		return buildNumbers;
	}

	@Override
	public Collection<Long> filterNumbers(Long projectId, Collection<String> commitHashes) {
		Collection<Long> buildNumbers = new HashSet<>();
		for (BuildFacade build: cache.values()) {
			if (build.getProjectId().equals(projectId) 
					&& commitHashes.contains(build.getCommitHash())) {
				buildNumbers.add(build.getNumber());
			}
		}
		return buildNumbers;
	}
	
	private void populateJobNames(Long projectId, String jobName) {
		Collection<String> jobNamesOfProject = jobNames.get(projectId);
		if (jobNamesOfProject == null) 
			jobNamesOfProject = new HashSet<>();
		jobNamesOfProject.add(jobName);
		jobNames.put(projectId, jobNamesOfProject);
	}

	@Override
	public Collection<String> getJobNames(@Nullable Project project) {
		Collection<String> jobNames = new HashSet<>();
		for (Map.Entry<Long, Collection<String>> entry: this.jobNames.entrySet()) { 
			if (project == null || project.getId().equals(entry.getKey()))
				jobNames.addAll(entry.getValue());
		}
		return jobNames;
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
		Map<Project, Collection<String>> accessibleJobNames = new HashMap<>();
		for (Long projectId: jobNames.keySet()) {
			Project project = projectManager.load(projectId);
			Collection<String> accessibleJobNamsOfProject = getAccessibleJobNames(project);
			if (!accessibleJobNamsOfProject.isEmpty())
				accessibleJobNames.put(project, accessibleJobNamsOfProject); 
		}
		return accessibleJobNames;
	}
	
	@Override
	public Collection<String> getAccessibleJobNames(Project project) {
		Collection<String> accessibleJobNames = new HashSet<>();
		Collection<String> availableJobNames = jobNames.get(project.getId());
		if (availableJobNames != null) {
			if (SecurityUtils.isAdministrator()) {
				accessibleJobNames.addAll(availableJobNames);
			} else {
				User user = SecurityUtils.getUser();
				if (user != null) {
					for (UserAuthorization authorization: user.getProjectAuthorizations()) {
						if (authorization.getProject().isSelfOrAncestorOf(project)) {
							populateAccessibleJobNames(accessibleJobNames, availableJobNames, 
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
								populateAccessibleJobNames(accessibleJobNames, availableJobNames, 
										authorization.getRole());
							}
						}
					}
				}
				
				Project current = project;
				do {
					Role defaultRole = current.getDefaultRole();
					if (defaultRole != null)
						populateAccessibleJobNames(accessibleJobNames, availableJobNames, defaultRole);
					current = current.getParent();
				} while (current != null);
			}
		}
		return accessibleJobNames;
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

	@Transactional
	@Override
	public void delete(Collection<Build> builds) {
		for (Build build: builds)
			delete(build);
	}

	@Sessional
	@Override
	public Collection<Build> query(Agent agent, Status status) {
		EntityCriteria<Build> criteria = EntityCriteria.of(Build.class);
		EntityCriteria<Build> agentCriteria = criteria.createCriteria(Build.PROP_AGENT, org.hibernate.sql.JoinType.INNER_JOIN);
		if (status != null) 
			agentCriteria.add(Restrictions.eq(Build.PROP_STATUS, status));
		return query(criteria);
	}

	@Sessional
	@Override
	public List<ProjectBuildStats> queryStats(Collection<Project> projects) {
		if (projects.isEmpty()) {
			return new ArrayList<>();
		} else {
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaQuery<ProjectBuildStats> criteriaQuery = builder.createQuery(ProjectBuildStats.class);
			Root<Build> root = criteriaQuery.from(Build.class);
			criteriaQuery.multiselect(
					root.get(Build.PROP_PROJECT).get(Project.PROP_ID), 
					root.get(Build.PROP_STATUS), 
					builder.count(root));
			criteriaQuery.groupBy(root.get(Build.PROP_PROJECT), root.get(Build.PROP_STATUS));
			
			criteriaQuery.where(root.get(Build.PROP_PROJECT).in(projects));
			criteriaQuery.orderBy(builder.asc(root.get(Build.PROP_STATUS)));
			
			return getSession().createQuery(criteriaQuery).getResultList();
		}
	}

	@Override
	public Build findByUUID(String uuid) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq(Issue.PROP_UUID, uuid));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Override
	public MimeFileInfo getArtifactInfo(Build build, String artifactPath) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<MimeFileInfo>() {

			private static final long serialVersionUID = 1L;

			@Override
			public MimeFileInfo call() throws Exception {
				File artifactsDir = Build.getArtifactsDir(projectId, buildNumber);
				File artifactFile = new File(artifactsDir, artifactPath);
				
				if (artifactFile.exists() && artifactFile.isFile()) {
					String mimeType = Files.probeContentType(artifactFile.toPath());
					if (mimeType == null)
						mimeType = MediaType.APPLICATION_OCTET_STREAM;
					return new MimeFileInfo(artifactPath, artifactFile.length(), 
							artifactFile.lastModified(), mimeType);
				} else {
					String errorMessage = String.format(
							"Specified artifact path does not exist or is a directory (project: %s, build number: %d, path: %s)", 
							projectId, buildNumber, artifactPath);
					throw new ExplicitException(errorMessage);
				}
			}
		});
	}

	@Override
	public void deleteArtifact(Build build, String artifactPath) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		projectManager.runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				return LockUtils.write(Build.getArtifactsLockName(projectId, buildNumber), new Callable<Void>() {

					@Override
					public Void call() throws Exception {
						File artifactsDir = Build.getArtifactsDir(projectId, buildNumber);
						File artifactFile = new File(artifactsDir, artifactPath);
						FileUtils.forceDelete(artifactFile);
						return null;
					}
					
				});				
			}
			
		});
	}

	@Override
	public List<FileInfo> listArtifacts(Build build, String artifactPath) {
		Long projectId = build.getProject().getId();
		Long buildNumber = build.getNumber();
		return projectManager.runOnProjectServer(projectId, new ClusterTask<List<FileInfo>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<FileInfo> call() throws Exception {
				return LockUtils.read(Build.getArtifactsLockName(projectId, buildNumber), new Callable<List<FileInfo>>() {

					@Override
					public List<FileInfo> call() throws Exception {
						List<FileInfo> files = new ArrayList<>();
						File artifactsDir = Build.getArtifactsDir(projectId, buildNumber);
						
						File directory = artifactsDir;
						if (artifactPath != null)
							directory = new File(artifactsDir, artifactPath);
						
						if (directory.exists()) {
							int baseLen = artifactsDir.getAbsolutePath().length() + 1;
							for (File file: directory.listFiles()) {
								files.add(new FileInfo(file.getAbsolutePath().substring(baseLen), 
										file.isFile()? file.length(): -1, file.lastModified()));
							}
						}
						return files;
					}
					
				});				
			}
			
		});
	}

}
