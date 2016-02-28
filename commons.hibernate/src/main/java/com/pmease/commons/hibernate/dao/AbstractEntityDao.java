package com.pmease.commons.hibernate.dao;

import java.util.List;

import org.hibernate.Session;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.util.ReflectionUtils;

public abstract class AbstractEntityDao<T extends AbstractEntity> implements EntityDao<T> {

	private final Class<T> entityClass;
	
	protected final Dao dao;
	
	@SuppressWarnings("unchecked")
	public AbstractEntityDao(Dao dao) {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(AbstractEntityDao.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			entityClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of entity dao implementation must "
					+ "be AbstractEntityDao and must realize the type argument <T>");
		}
		this.dao = dao;
    }
	
	@Override
	public T get(Long entityId) {
		return dao.get(entityClass, entityId);
	}

	@Override
	public T load(Long entityId) {
		return dao.load(entityClass, entityId);
	}

	@Override
	public void persist(AbstractEntity entity) {
		dao.persist(entity);
	}

	@Override
	public void remove(AbstractEntity entity) {
		dao.remove(entity);
	}

	@Override
	public List<T> query(EntityCriteria<T> criteria, int firstResult, int maxResults) {
		return dao.query(criteria, firstResult, maxResults);
	}

	@Override
	public List<T> query(EntityCriteria<T> criteria) {
		return dao.query(criteria);
	}

	@Override
	public List<T> all() {
		return dao.allOf(entityClass);
	}

	@Override
	public T find(EntityCriteria<T> entityCriteria) {
		return dao.find(entityCriteria);
	}

	@Override
	public int count(EntityCriteria<T> detachedCriteria) {
		return dao.count(detachedCriteria);
	}

	protected void afterCommit(Runnable runnable) {
		dao.afterCommit(runnable);
	}

	protected Session getSession() {
		return dao.getSession();
	}

}
