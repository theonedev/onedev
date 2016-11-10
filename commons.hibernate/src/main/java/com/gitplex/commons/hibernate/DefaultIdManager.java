package com.gitplex.commons.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.metadata.ClassMetadata;

import com.gitplex.commons.hibernate.dao.Dao;

@Singleton
public class DefaultIdManager implements IdManager {

	private final Dao dao;
	
	private final PersistManager persistManager;
	
	private final Map<Class<?>, AtomicLong> nextIds = new HashMap<>();
	
	@Inject
	public DefaultIdManager(Dao dao, PersistManager persistManager) {
		this.dao = dao;
		this.persistManager = persistManager;
	}

	private long getMaxId(Class<?> entityClass) {
		Criteria criteria = dao.getSession().createCriteria(entityClass);
		criteria.setProjection(Projections.max("id"));
		Long maxId = (Long) criteria.uniqueResult();
		if (maxId == null)
			maxId = 0L;
		return maxId;
	}
	
	@Sessional
	@Override
	public void init() {
		for (ClassMetadata metadata: persistManager.getSessionFactory().getAllClassMetadata().values()) {
			Class<?> entityClass = metadata.getMappedClass();
			nextIds.put(entityClass, new AtomicLong(getMaxId(entityClass)+1));
		}
	}

	@Sessional
	@Override
	public void init(Class<?> entityClass) {
		nextIds.get(entityClass).set(getMaxId(entityClass)+1);
	}

	@Override
	public long nextId(Class<?> entityClass) {
		return nextIds.get(entityClass).getAndIncrement();
	}

}
