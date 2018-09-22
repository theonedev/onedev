package io.onedev.server.manager.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import io.onedev.launcher.loader.Listen;
import io.onedev.launcher.loader.ListenerRegistry;
import io.onedev.server.OneDev;
import io.onedev.server.event.build.BuildFinished;
import io.onedev.server.event.build.BuildStarted;
import io.onedev.server.event.lifecycle.SystemStarted;
import io.onedev.server.event.lifecycle.SystemStopping;
import io.onedev.server.manager.BuildManager;
import io.onedev.server.manager.CacheManager;
import io.onedev.server.manager.ConfigurationManager;
import io.onedev.server.model.Build;
import io.onedev.server.model.Configuration;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.build.BuildConstants;
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
import io.onedev.utils.schedule.SchedulableTask;
import io.onedev.utils.schedule.TaskScheduler;

@Singleton
public class DefaultBuildManager extends AbstractEntityManager<Build> implements BuildManager, SchedulableTask {

	private final TaskScheduler taskScheduler;
	
	private final ListenerRegistry listenerRegistry;
	
	private final ConfigurationManager configurationManager;
	
	private String taskId;
	
	@Inject
	public DefaultBuildManager(Dao dao, ListenerRegistry listenerRegistry, TaskScheduler taskScheduler, 
			ConfigurationManager configurationManager) {
		super(dao);
		this.listenerRegistry = listenerRegistry;
		this.taskScheduler = taskScheduler;
		this.configurationManager = configurationManager;
	}

	@Transactional
	@Override
	public void delete(Build build) {
    	Query<?> query = getSession().createQuery("update PullRequestBuild set build=null where build=:build");
    	query.setParameter("build", build);
    	query.executeUpdate();
    	
    	super.delete(build);
	}

	@Sessional
	@Override
	public List<Build> query(Project project, String commitHash) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
		criteria.add(Restrictions.eq("commitHash", commitHash));
		criteria.addOrder(Order.asc("id"));
		return query(criteria);
	}

	@Override
	public Build findByCommit(Configuration configuration, String commitHash) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("configuration", configuration));
		criteria.add(Restrictions.eq("commitHash", commitHash));
		return find(criteria);
	}

	@Override
	public void save(Build build) {
		super.save(build);
		
		if (build.getStatus() == Build.Status.RUNNING) 
			listenerRegistry.post(new BuildStarted(build));
		else
			listenerRegistry.post(new BuildFinished(build));
	}
	
	@Sessional
	@Override
	public Build findByVersion(Configuration configuration, String version) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.add(Restrictions.eq("configuration", configuration));
		criteria.add(Restrictions.eq("version", version));
		criteria.addOrder(Order.desc("id"));
		return find(criteria);
	}
	
	@Sessional
	@Override
	public List<Build> query(Project project, String term, int count) {
		List<Build> builds = new ArrayList<>();
		
		EntityCriteria<Build> criteria = newCriteria();
		Criteria configurationCriteria = criteria.createCriteria("configuration");
		configurationCriteria.add(Restrictions.eq("project", project));
		if (term != null) {
			if (term.contains(Build.FQN_SEPARATOR)) {
				String configurationTerm = StringUtils.substringBefore(term, Build.FQN_SEPARATOR);
				String versionTerm = StringUtils.substringAfter(term, Build.FQN_SEPARATOR);
				configurationCriteria.add(Restrictions.ilike("name", "%" + configurationTerm + "%"));
				criteria.add(Restrictions.ilike("version", "%" + versionTerm + "%"));
			} else {
				criteria.add(Restrictions.ilike("version", "%" + term + "%"));
			}
		}
		criteria.addOrder(Order.desc("id"));
		builds.addAll(query(criteria, 0, count-builds.size()));
		return builds;
	}

	@Override
	public List<Build> queryAfter(Project project, Long afterBuildId, int count) {
		EntityCriteria<Build> criteria = newCriteria();
		criteria.createCriteria("configuration").add(Restrictions.eq("project", project));
		criteria.addOrder(Order.asc("id"));
		if (afterBuildId != null)
			criteria.add(Restrictions.gt("id", afterBuildId));
		return query(criteria, 0, count);
	}

	@Sessional
	@Override
	public Build findByFQN(Project project, String fqn) {
		String configurationName = StringUtils.substringBefore(fqn, Build.FQN_SEPARATOR);
		String buildVersion = StringUtils.substringAfter(fqn, Build.FQN_SEPARATOR);
		Configuration configuration = OneDev.getInstance(ConfigurationManager.class).find(project, configurationName);
		if (configuration != null)
			return OneDev.getInstance(BuildManager.class).findByVersion(configuration, buildVersion);
		else
			return null;
	}

	private Predicate[] getPredicates(io.onedev.server.search.entity.EntityCriteria<Build> criteria, Project project, 
			QueryBuildContext<Build> context, User user) {
		List<Predicate> predicates = new ArrayList<>();
		Join<?, ?> join = context.getRoot().join(BuildConstants.ATTR_CONFIGURATION, JoinType.INNER);
		join.on(context.getBuilder().equal(join.get(Configuration.ATTR_PROJECT), project));
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

		Path<String> idPath = root.get("id");
		if (orders.isEmpty())
			orders.add(builder.desc(idPath));
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

	@Transactional
	@Override
	public void cleanupBuilds(Configuration configuration) {
		configuration.getBuildCleanupRule().cleanup(configuration, getSession());
	}

	@Listen
	public void on(SystemStarted event) {
		taskId = taskScheduler.schedule(this);
	}

	@Listen
	public void on(SystemStopping event) {
		taskScheduler.unschedule(taskId);
	}

	@Override
	public void execute() {
		for (Long configurationId: OneDev.getInstance(CacheManager.class).getConfigurations().keySet())
			cleanup(configurationId);
	}
	
	@Transactional
	protected void cleanup(Long configurationId) {
		Configuration configuration = configurationManager.load(configurationId);
		cleanupBuilds(configuration);
	}

	@Override
	public ScheduleBuilder<?> getScheduleBuilder() {
		return CronScheduleBuilder.cronSchedule("0 0 1 * * ?");
	}
	
}
