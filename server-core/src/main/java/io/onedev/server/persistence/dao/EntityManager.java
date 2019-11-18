package io.onedev.server.persistence.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;

import io.onedev.server.model.AbstractEntity;

public interface EntityManager<T extends AbstractEntity> {
	
	/**
	 * Get the entity with the specified type and id from data store.
	 * 
	 * @param entityId
	 * 			identifier of the entity to be found
	 * @return
	 *			found entity object, null if not found 			
	 */
	T get(Long entityId);

	/**
	 * Get a reference to the entity with the specified type and id from data store.
	 * <p>
	 * This does not require a call to the datastore and does not populate any
	 * of the entity's values. Values may be fetched lazily at a later time.
	 * This increases performance if a another entity is being saved that should
	 * reference this entity but the values of this entity are not needed.
	 * 
	 * @throws
	 *             HibernateException if no matching entity is found
	 */
	T load(Long entityId);

	/**
	 * If the id of the entity is null or zero, add it to the datastore and
	 * assign it an id; otherwise, update the corresponding entity in the
	 * data store with the properties of this entity. In either case the entity
	 * passed to this method will be attached to the session.
	 * <p>
	 * If an entity to update is already attached to the session, this method
	 * will have no effect. If an entity to update has the same id as another
	 * instance already attached to the session, an error will be thrown.
	 * 
	 * @param entity
	 * 			the entity to be saved
	 */
	void save(T entity);

	/**
	 * Remove the specified entity from the datastore.
	 * 
	 * @param entity
	 * 			the entity to be deleted
	 */
	void delete(T entity);

	/**
	 * Query with specified criteria.
	 * 
	 * @param criteria
	 * 			hibernate {@link DetachedCriteria}
	 * @param firstResult
	 * 			first result of the query. Set to 0 if this value should be ignored
	 * @param maxResults
	 * 			max number of returned results. Set to 0 if no limit of the max results should be set 
	 * @return
	 * 			a list of execution result of the detached criteria
	 */
	List<T> query(EntityCriteria<T> criteria, int firstResult, int maxResults);
	
	List<T> query(EntityCriteria<T> criteria);
	
	List<T> query(boolean cacheable);
	
	List<T> query();
	
	int count(boolean cacheable);
	
	int count();
	
	EntityCriteria<T> newCriteria();
	
	/**
	 * This method expects to lookup a single entity with specified criteria
	 * 
	 * @param detachedCriteria
	 * 			Hibernate {@link DetachedCriteria}
	 * @return
	 * 			the first matching entity, or null if not found 
	 */
	T find(EntityCriteria<T> entityCriteria);
	
	/**
	 * Count entities of specified class matching specified criteria. 
	 * 
	 * @return
	 * 			number of entities matching specified {@link DetachedCriteria}
	 */
	int count(EntityCriteria<T> entityCriteria);

}
