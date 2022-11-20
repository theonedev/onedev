package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.event.Listen;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.BaseEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildParamManager extends BaseEntityManager<BuildParam> implements BuildParamManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildParamManager.class);
	
	private final TransactionManager transactionManager;
	
	private final ClusterManager clusterManager;
	
	private volatile Map<Long, Collection<String>> paramNames;
	
	@Inject
	public DefaultBuildParamManager(Dao dao, TransactionManager transactionManager, ClusterManager clusterManager) {
		super(dao);
		this.transactionManager = transactionManager;
		this.clusterManager = clusterManager;
	}

	@Transactional
	@Override
	public void deleteParams(Build build) {
		Query<?> query = getSession().createQuery("delete from BuildParam where build = :build");
		query.setParameter("build", build);
		query.executeUpdate();
		build.getParams().clear();
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Listen
	public void on(SystemStarted event) {
		logger.info("Caching build param info...");

		Map<Long, Long> projectIds = new HashMap<>();
		Query<?> query = dao.getSession().createQuery("select id, project.id from Build");
		for (Object[] fields: (List<Object[]>)query.list()) 
			projectIds.put((Long)fields[0], (Long)fields[1]);
		
		paramNames = clusterManager.getHazelcastInstance().getReplicatedMap("buildParamNames");
		
		query = dao.getSession().createQuery("select build.id, name from BuildParam");
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long projectId = projectIds.get(fields[0]);
			if (projectId != null)
				addParam(projectId, (String) fields[1]);
		}
	}

	@Transactional
	@Override
	public void save(BuildParam param) {
		super.save(param);
		
		Long projectId = param.getBuild().getProject().getId();
		String paramName = param.getName();
		
		transactionManager.runAfterCommit(new Runnable() {

			@Override
			public void run() {
				addParam(projectId, paramName);
			}
			
		});
	}

	private void addParam(Long projectId, String paramName) {
		Collection<String> paramsOfProject = paramNames.get(projectId);
		if (paramsOfProject == null) 
			paramsOfProject = new HashSet<>();
		paramsOfProject.add(paramName);
		paramNames.put(projectId, paramsOfProject);
	}
	
	@Override
	public Collection<String> getParamNames(@Nullable Project project) {
		Collection<String> paramNames = new HashSet<>();
		for (Map.Entry<Long, Collection<String>> entry: this.paramNames.entrySet()) {
			if (project == null || project.getId().equals(entry.getKey()))
				paramNames.addAll(entry.getValue());
		}
		return paramNames;
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Project) {
			Long projectId = event.getEntity().getId();
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					paramNames.remove(projectId);
				}
				
			});
		}
	}
	
}
