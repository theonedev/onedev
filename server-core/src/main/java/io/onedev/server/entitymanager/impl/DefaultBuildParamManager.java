package io.onedev.server.entitymanager.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.launcher.loader.Listen;
import io.onedev.server.ci.job.paramspec.ParamSpec;
import io.onedev.server.entitymanager.BuildParamManager;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.model.Build;
import io.onedev.server.model.BuildParam;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;

@Singleton
public class DefaultBuildParamManager extends AbstractEntityManager<BuildParam> implements BuildParamManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBuildParamManager.class);
	
	private static final int MAX_PARAM_VALUES = 100; 
	
	private final TransactionManager transactionManager;
	
	private final Map<String, Set<String>> buildParams = new HashMap<>();
	
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
		
		Query<?> query = dao.getSession().createQuery("select distinct name, type, value, id from BuildParam order by id");
		for (Object[] fields: (List<Object[]>)query.list()) {
			if (!fields[1].equals(ParamSpec.SECRET))
				addBuildParam((String) fields[0], (String) fields[2]);
		}
	}

	@Transactional
	@Override
	public void save(BuildParam param) {
		super.save(param);
		
		if (!param.getType().equals(ParamSpec.SECRET)) {
			String paramName = param.getName();
			String paramValue = param.getValue();
			
			transactionManager.runAfterCommit(new Runnable() {

				@Override
				public void run() {
					buildParamsLock.writeLock().lock();
					try {
						if (!param.getType().equals(ParamSpec.SECRET))
							addBuildParam(paramName, paramValue);
					} finally {
						buildParamsLock.writeLock().unlock();
					}
				}
				
			});
		}
	}

	private void addBuildParam(String name, String value) {
		Set<String> values = buildParams.get(name);
		if (values == null) {
			values = new LinkedHashSet<>();
			buildParams.put(name, values);
		}
		values.add(value);
		if (values.size() > MAX_PARAM_VALUES)
			values.iterator().remove();
	}
	
	@Override
	public Collection<String> getBuildParamNames() {
		buildParamsLock.readLock().lock();
		try {
			return new HashSet<>(buildParams.keySet());
		} finally {
			buildParamsLock.readLock().unlock();
		}
	}
	
	@Override
	public Collection<String> getBuildParamValues(String paramName) {
		buildParamsLock.readLock().lock();
		try {
			Set<String> paramValues = buildParams.get(paramName);
			if (paramValues != null) 
				return new HashSet<>(paramValues);
			else 
				return new HashSet<>();
		} finally {
			buildParamsLock.readLock().unlock();
		}
	}

}
