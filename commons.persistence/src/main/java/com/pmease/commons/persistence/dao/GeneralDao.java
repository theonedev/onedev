package com.pmease.commons.persistence.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;

import com.pmease.commons.persistence.AbstractEntity;

public interface GeneralDao {
	
	/**
	 * Find the entity with the specified type and id from data store.
	 * 
	 * @param entityClass 
	 * 			class of the entity to be found
	 * @param entityId
	 * 			identifier of the entity to be found
	 * @return
	 *			found entity object, null if not found 			
	 */
	<T extends AbstractEntity> T find(Class<T> entityClass, Long entityId);

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
	<T extends AbstractEntity> T load(Class<T> entityClass, Long entityId);

	/**
	 * If the id of the entity is null or zero, add it to the datastore and
	 * assign it an id; otherwise, update the corresponding entity in the
	 * datastore with the properties of this entity. In either case the entity
	 * passed to this method will be attached to the session.
	 * <p>
	 * If an entity to update is already attached to the session, this method
	 * will have no effect. If an entity to update has the same id as another
	 * instance already attached to the session, an error will be thrown.
	 * 
	 * @param entity
	 * 			the entity to be saved
	 */
	void save(AbstractEntity entity);

	/**
	 * Remove the specified entity from the datastore.
	 * 
	 * @param entity
	 * 			the entity to be deleted
	 */
	void delete(AbstractEntity entity);
	
	/**
	 * Delete entity of specified class and identifier without actually loading the entity.
	 * 
	 * @param entityClass
	 * 			class of the entity
	 * @param entityId
	 * 			identifier of the entity
	 */
	<T extends AbstractEntity> void deleteById(Class<T> entityClass, Long entityId);

	/**
	 * Search with specified criteria.
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
	List<?> search(DetachedCriteria criteria, int firstResult, int maxResults);

	/**
	 * This method expects to find a single entity with specified criteria
	 * 
	 * @param detachedCriteria
	 * 			Hibernate {@link DetachedCriteria}
	 * @return
	 * 			the single entity. null if not found
	 * @throws 
	 * 			HibernateException if there is more than one matching result
	 */
	Object find(DetachedCriteria detachedCriteria);
	
	/**
	 * Count entities of specified class matching specified criteria. 
	 * 
	 * @return
	 * 			number of entities matching specified {@link DetachedCriteria}
	 */
	<T extends AbstractEntity> int count(DetachedCriteria detachedCriteria);
	
}
