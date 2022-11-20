package io.onedev.server.entitymanager.impl;

import static io.onedev.commons.utils.ExceptionUtils.unchecked;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.BuildMetricManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Build;
import io.onedev.server.model.Group;
import io.onedev.server.model.GroupAuthorization;
import io.onedev.server.model.Project;
import io.onedev.server.model.Role;
import io.onedev.server.model.User;
import io.onedev.server.model.UserAuthorization;
import io.onedev.server.model.support.BuildMetric;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.search.buildmetric.BuildMetricQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.security.permission.AccessBuildReports;
import io.onedev.server.security.permission.JobPermission;
import io.onedev.server.util.BeanUtils;
import io.onedev.server.util.MetricIndicator;

@Singleton
public class DefaultBuildMetricManager implements BuildMetricManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildMetricManager.class);
	
	private final Dao dao;
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Map<Key, Map<String, Collection<String>>> reportNames;
	
	@Inject
	public DefaultBuildMetricManager(Dao dao, TransactionManager transactionManager, ClusterManager clusterManager) {
		this.dao = dao;
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
	}
	
	@SuppressWarnings("resource")
	@Sessional
	@Override
	public <T extends AbstractEntity> Map<Integer, T> queryStats(Project project, Class<T> metricClass, BuildMetricQuery query) {
		CriteriaBuilder builder = dao.getSession().getCriteriaBuilder();
		CriteriaQuery<Object[]> criteriaQuery = builder.createQuery(Object[].class);
		Root<T> metricRoot = criteriaQuery.from(metricClass);

		List<Predicate> predicates = new ArrayList<>();
		
		Join<?, ?> buildJoin = metricRoot.join(BuildMetric.PROP_BUILD, JoinType.INNER);
		predicates.add(builder.equal(buildJoin.get(Build.PROP_PROJECT), project));
		
		if (!SecurityUtils.canManageBuilds(project)) {
			Key key = new Key(project.getId(), metricClass);
			Map<String, Collection<String>> availableReportNames = reportNames.get(key);
			if (availableReportNames != null) {
				List<Predicate> jobPredicates = new ArrayList<>();
				Collection<String> jobsWithAllReports = new HashSet<>();
				for (Map.Entry<String, Collection<String>> entry: getAccessibleReportNames(project, metricClass).entrySet()) {
					Collection<String> availableReportNamesOfJob = availableReportNames.get(entry.getKey());
					if (availableReportNamesOfJob != null) {
						if (entry.getValue().containsAll(availableReportNamesOfJob)) {
							jobsWithAllReports.add(entry.getKey());
							jobPredicates.add(builder.equal(buildJoin.get(Build.PROP_JOB), entry.getKey()));
						} else {
							List<Predicate> reportPredicates = new ArrayList<>();
							for (String reportName: entry.getValue()) 
								reportPredicates.add(builder.equal(metricRoot.get(BuildMetric.PROP_REPORT), reportName));
							jobPredicates.add(builder.and(
									builder.equal(buildJoin.get(Build.PROP_JOB), entry.getKey()), 
									builder.or(reportPredicates.toArray(new Predicate[reportPredicates.size()]))));
						}
					} else {
						jobsWithAllReports.add(entry.getKey());
					}
				}
				if (!jobsWithAllReports.containsAll(availableReportNames.keySet()))
					predicates.add(builder.or(jobPredicates.toArray(new Predicate[jobPredicates.size()])));
			}
		}
		
		if (query.getCriteria() != null)
			predicates.add(query.getCriteria().getPredicate(metricRoot, buildJoin, builder));
		
		criteriaQuery.where(predicates.toArray(new Predicate[0]));
		criteriaQuery.groupBy(buildJoin.get(Build.PROP_FINISH_DAY));
		
		List<Method> setters = new ArrayList<>();
		List<Selection<?>> selections = new ArrayList<>();
		for (Method getter: BeanUtils.findGetters(metricClass)) {
			if (getter.getAnnotation(MetricIndicator.class) != null) {
				selections.add(builder.avg(metricRoot.get(BeanUtils.getPropertyName(getter))));
				setters.add(Preconditions.checkNotNull(BeanUtils.findSetter(getter)));
			}
		}
		
		selections.add(buildJoin.get(Build.PROP_FINISH_DAY));
		
		criteriaQuery.multiselect(selections);
		
		Map<Integer, T> metrics = new HashMap<>();
		
		for (Object[] fields: dao.getSession().createQuery(criteriaQuery).list()) {
			try {
				T metric = metricClass.getDeclaredConstructor().newInstance();
				int index = 0;
				for (Method setter: setters) {
					double value = (double)fields[index++];
					setter.invoke(metric, (int)value);
				}
				metrics.put((int)fields[setters.size()], metric);
			} catch (Exception e) {
				throw unchecked(e);
			}
		}
		return metrics;
	}

	@SuppressWarnings("unchecked")
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching build metric info...");
		
		reportNames = clusterManager.getHazelcastInstance().getReplicatedMap("buildReportNames");
		
		EntityManagerFactory emf = (EntityManagerFactory)dao.getSession().getEntityManagerFactory();
		for (EntityType<?> entityType: emf.getMetamodel().getEntities()) {
			Class<?> entityClass = entityType.getJavaType();
			if (BuildMetric.class.isAssignableFrom(entityClass)) {
				String queryString = String.format("select build.%s.id, build.%s, metric.%s from %s metric inner join metric.%s build", 
						Build.PROP_PROJECT, Build.PROP_JOB, BuildMetric.PROP_REPORT, entityClass.getSimpleName(), BuildMetric.PROP_BUILD);
				Query<?> query = dao.getSession().createQuery(queryString);
				for (Object[] fields: (List<Object[]>)query.list()) 
					populateReportNames(new Key((Long)fields[0], entityClass), (String)fields[1], (String)fields[2]);
			}
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					for (var key: reportNames.entrySet().stream()
							.map(it->it.getKey())
							.filter(it->it.projectId.equals(projectId))
							.collect(Collectors.toSet())) {
						reportNames.remove(key);
					}
				}
				
			});
		}
	}
	
	@Transactional
	@Listen
	public void on(EntityPersisted event) {
		if (event.getEntity() instanceof BuildMetric) {
			BuildMetric buildMetric = (BuildMetric) event.getEntity();
			String reportName = buildMetric.getReportName();
			String jobName = buildMetric.getBuild().getJobName();
			Key key = new Key(buildMetric.getBuild().getProject().getId(), event.getEntity().getClass());
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					populateReportNames(key, jobName, reportName);
				}
				
			});
		}
	}
	
	private void populateReportNames(Key key, String jobName, String reportName) {
		Map<String, Collection<String>> reportNamesOfKey = reportNames.get(key);
		if (reportNamesOfKey == null) 
			reportNamesOfKey = new HashMap<>();
		Collection<String> reportNamesOfJob = reportNamesOfKey.get(jobName);
		if (reportNamesOfJob == null) {
			reportNamesOfJob = new HashSet<>();
			reportNamesOfKey.put(jobName, reportNamesOfJob);
		}
		reportNamesOfJob.add(reportName);
		reportNames.put(key, reportNamesOfKey);
	}
	
	private void populateAccessibleReportNames(Map<String, Collection<String>> accessibleReportNames, 
			Map<String, Collection<String>> availableReportNames, Project project, Role role) {
		for (Map.Entry<String, Collection<String>> entry: availableReportNames.entrySet()) {
			String jobName = entry.getKey();
			for (String reportName: entry.getValue()) {
				if (role.implies(new JobPermission(jobName, new AccessBuildReports(reportName)))) {
					Collection<String> accessibleReportNamesOfJob = accessibleReportNames.get(jobName);
					if (accessibleReportNamesOfJob == null) {
						accessibleReportNamesOfJob = new HashSet<>();
						accessibleReportNames.put(jobName, accessibleReportNamesOfJob);
					}
					accessibleReportNamesOfJob.add(reportName);
				}
			}
		}
	}
	
	@Override
	public Map<String, Collection<String>> getAccessibleReportNames(Project project, Class<?> metricClass) {
		Map<String, Collection<String>> accessibleReportNames = new HashMap<>();
		Key key = new Key(project.getId(), metricClass);
		Map<String, Collection<String>> availableReportNames = reportNames.get(key);
		if (availableReportNames != null) {
			if (SecurityUtils.isAdministrator()) {
				for (Map.Entry<String, Collection<String>> entry: availableReportNames.entrySet())
					accessibleReportNames.put(entry.getKey(), new HashSet<>(entry.getValue()));
			} else {
				User user = SecurityUtils.getUser();
				if (user != null) {
					for (UserAuthorization authorization: user.getProjectAuthorizations()) {
						if (project.equals(authorization.getProject())) {
							populateAccessibleReportNames(accessibleReportNames, availableReportNames, 
									project, authorization.getRole());
						}
					}
					for (Group group: user.getGroups()) {
						for (GroupAuthorization authorization: group.getAuthorizations()) {
							if (project.equals(authorization.getProject())) {
								populateAccessibleReportNames(accessibleReportNames, availableReportNames, 
										project, authorization.getRole());
							}
						}
					}
				}
				if (project.getDefaultRole() != null) {
					populateAccessibleReportNames(accessibleReportNames, availableReportNames, 
							project, project.getDefaultRole());
				}
			}				
		}
		return accessibleReportNames;
	}
	
	private static class Key implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final Long projectId;
		
		private final Class<?> metricClass;
		
		public Key(Long projectId, Class<?> metricClass) {
			this.projectId = projectId;
			this.metricClass = metricClass;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Key)) 
				return false;
			if (this == other)
				return true;
			Key otherKey = (Key) other;
			return new EqualsBuilder()
				.append(projectId, otherKey.projectId)
				.append(metricClass, otherKey.metricClass)
				.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
				.append(projectId)
				.append(metricClass)
				.toHashCode();
		}		
		
	}
	
}
