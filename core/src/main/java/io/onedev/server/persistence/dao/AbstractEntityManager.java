package io.onedev.server.persistence.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Session;
import org.hibernate.query.Query;

import io.onedev.server.model.AbstractEntity;
import io.onedev.server.model.Project;
import io.onedev.utils.ReflectionUtils;

public abstract class AbstractEntityManager<T extends AbstractEntity> implements EntityManager<T> {

	private final Class<T> entityClass;
	
	protected final Dao dao;
	
	private final Map<String, AtomicLong> nextNumbers = new HashMap<>();
	
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
	
	protected long getNextNumber(Project project) {
		AtomicLong nextNumber;
		synchronized (nextNumbers) {
			nextNumber = nextNumbers.get(project.getUUID());
		}
		if (nextNumber == null) {
			long maxNumber;
			Query<?> query = getSession().createQuery("select max(number) from PullRequest where targetProject=:project");
			query.setParameter("project", project);
			Object result = query.uniqueResult();
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
				nextNumber = nextNumbers.get(project.getUUID());
				if (nextNumber == null) {
					nextNumber = new AtomicLong(maxNumber+1);
					nextNumbers.put(project.getUUID(), nextNumber);
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
	public List<T> query() {
		return dao.query(entityClass);
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
