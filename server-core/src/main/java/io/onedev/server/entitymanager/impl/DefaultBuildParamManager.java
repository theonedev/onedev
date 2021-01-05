package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.entitymanager.BuildParamManager;
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
	
	private final Map<Long, Collection<String>> buildParams = new HashMap<>();
	
	private final ReadWriteLock buildParamsLock = new ReentrantReadWriteLock();
	
	@Inject
	public DefaultBuildParamManager(Dao dao, TransactionManager transactionManager) {
		super(dao);
		this.transactionManager = transactionManager;
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
		
		query = dao.getSession().createQuery("select build.id, name from BuildParam where type != :secret");
		query.setParameter("secret", ParamSpec.SECRET);
		for (Object[] fields: (List<Object[]>)query.list()) {
			Long projectId = projectIds.get(fields[0]);
			if (projectId != null)
				addBuildParam(projectId, (String) fields[1]);
		}
	}

	@Transactional
	@Override
	public void save(BuildParam param) {
		super.save(param);
		
		if (!param.getType().equals(ParamSpec.SECRET)) {
			Long projectId = param.getBuild().getProject().getId();
			String paramName = param.getName();
			
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					buildParamsLock.writeLock().lock();
					try {
						addBuildParam(projectId, paramName);
					} finally {
						buildParamsLock.writeLock().unlock();
					}
				}
				
			});
		}
	}

	private void addBuildParam(Long projectId, String paramName) {
		Collection<String> paramsOfProject = buildParams.get(projectId);
		if (paramsOfProject == null) {
			paramsOfProject = new HashSet<>();
			buildParams.put(projectId, paramsOfProject);
		}
		paramsOfProject.add(paramName);
	}
	
	@Override
	public Collection<String> getBuildParamNames(@Nullable Project project) {
		buildParamsLock.readLock().lock();
		try {
			Collection<String> buildParams = new HashSet<>();
			for (Map.Entry<Long, Collection<String>> entry: this.buildParams.entrySet()) {
				if (project == null || project.getId().equals(entry.getKey()))
					buildParams.addAll(entry.getValue());
			}
			return buildParams;
		} finally {
			buildParamsLock.readLock().unlock();
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
					buildParamsLock.writeLock().lock();
					try {
						for (Iterator<Map.Entry<Long, Collection<String>>> it = buildParams.entrySet().iterator(); it.hasNext();) {
							if (it.next().getKey().equals(projectId))
								it.remove();
						}
					} finally {
						buildParamsLock.writeLock().unlock();
					}
				}
			});
		}
	}
	
}
