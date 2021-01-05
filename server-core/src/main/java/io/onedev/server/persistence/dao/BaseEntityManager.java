package io.onedev.server.persistence.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Session;
import org.hibernate.query.Query;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.server.util.ReflectionUtils;

public abstract class BaseEntityManager<T extends AbstractEntity> implements EntityManager<T> {

	private final Class<T> entityClass;
	
	protected final Dao dao;
	
	private final Map<Long, AtomicLong> nextNumbers = new HashMap<>();
	
	@SuppressWarnings("unchecked")
	public BaseEntityManager(Dao dao) {
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(BaseEntityManager.class, getClass());
		if (typeArguments.size() == 1 && AbstractEntity.class.isAssignableFrom(typeArguments.get(0))) {
			entityClass = (Class<T>) typeArguments.get(0);
		} else {
			throw new RuntimeException("Super class of entity manager implementation must "
					+ "be AbstractEntityManager and must realize the type argument <T>");
		}
		this.dao = dao;
    }
	
	protected long getNextNumber(Project numberScope, Query<?> maxNumberQuery) {
		AtomicLong nextNumber;
		synchronized (nextNumbers) {
			nextNumber = nextNumbers.get(numberScope.getId());
		}
		if (nextNumber == null) {
			long maxNumber;
			Object result = maxNumberQuery.uniqueResult();
			if (result != null) {
				maxNumber = (Long)result;
			} else {
				maxNumber = 0;
			}
			
			/*
			 * do not put the whole method in synchronized block to avoid possible deadlocks
			 * if there are limited connections. 
			 */
			synchronized (nextNumbers) {
				nextNumber = nextNumbers.get(numberScope.getId());
				if (nextNumber == null) {
					nextNumber = new AtomicLong(maxNumber+1);
					nextNumbers.put(numberScope.getId(), nextNumber);
				}
			}
		} 
		return nextNumber.getAndIncrement();
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
