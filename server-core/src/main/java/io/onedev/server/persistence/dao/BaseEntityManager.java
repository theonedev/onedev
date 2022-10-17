package io.onedev.server.persistence.dao;

import java.util.List;

import org.hibernate.Session;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.util.ReflectionUtils;

public abstract class BaseEntityManager<T extends AbstractEntity> implements EntityManager<T> {

	private final Class<T> entityClass;
	
	protected final Dao dao;
	
	@SuppressWarnings("unchecked")
	public BaseEntityManager(Dao dao) {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(BaseEntityManager.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			entityClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of entity manager implementation must "
					+ "be BaseEntityManager and must realize the type argument <T>");
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
	public List<T> query(EntityCriteria<T> criteria, int firstResult, int maxResults) {
		return dao.query(criteria, firstResult, maxResults);
	}

	@Override
	public List<T> query(EntityCriteria<T> criteria) {
		return dao.query(criteria);
	}

	@Override
	public List<T> query(boolean cacheable) {
		return dao.query(entityClass, cacheable);
	}

	@Override
	public List<T> query() {
		return query(false);
	}
	
	@Override
	public int count(boolean cacheable) {
		return dao.count(entityClass, cacheable);
	}

	@Override
	public int count() {
		return count(false);
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

	public Class<T> getEntityClass() {
		return entityClass;
	}
	
}
