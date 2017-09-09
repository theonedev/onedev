package com.gitplex.server.persistence.dao;

import java.util.List;

import org.hibernate.Session;

import com.gitplex.server.model.AbstractEntity;
import com.gitplex.server.util.ReflectionUtils;

public abstract class AbstractEntityManager<T extends AbstractEntity> implements EntityManager<T> {

	private final Class<T> entityClass;
	
	protected final Dao dao;
	
	@SuppressWarnings("unchecked")
	public AbstractEntityManager(Dao dao) {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(AbstractEntityManager.class, getClass());
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
	public void save(T entity) {
		dao.persist(entity);
	}

	@Override
	public void delete(T entity) {
		dao.remove(entity);
	}

	@Override
	public EntityCriteria<T> newCriteria() {
		return EntityCriteria.<T>of(entityClass);
	}

	@Override
	public List<T> findRange(EntityCriteria<T> criteria, int firstResult, int maxResults) {
		return dao.findRange(criteria, firstResult, maxResults);
	}

	@Override
	public List<T> findAll(EntityCriteria<T> criteria) {
		return dao.findAll(criteria);
	}

	@Override
	public List<T> findAll() {
		return dao.findAll(entityClass);
	}

	@Override
	public T find(EntityCriteria<T> entityCriteria) {
		return dao.find(entityCriteria);
	}

	@Override
	public int count(EntityCriteria<T> detachedCriteria) {
		return dao.count(detachedCriteria);
	}

	protected Session getSession() {
		return dao.getSession();
	}

}
