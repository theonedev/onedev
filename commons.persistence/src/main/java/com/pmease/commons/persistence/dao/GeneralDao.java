package com.pmease.commons.persistence.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.criterion.DetachedCriteria;

import com.pmease.commons.persistence.AbstractEntity;

public interface GeneralDao {
	/**
	 * Get the entity with the specified type and id from the datastore.
	 * If none is found, return null.
	 */
	<T extends AbstractEntity> T find(Class<T> entityClass, Long entityId);

	/**
	 * Get a reference to the entity with the specified type and id from the
	 * datastore.
	 * 
	 * This does not require a call to the datastore and does not populate any
	 * of the entity's values. Values may be fetched lazily at a later time.
	 * This increases performance if a another entity is being saved that should
	 * reference this entity but the values of this entity are not needed.
	 * 
	 * @throws a
	 *             HibernateException if no matching entity is found
	 */
	<T extends AbstractEntity> T load(Class<T> entityClass, Long entityId);

	/**
	 * If the id of the entity is null or zero, add it to the datastore and
	 * assign it an id; otherwise, update the corresponding entity in the
	 * datastore with the properties of this entity. In either case the entity
	 * passed to this method will be attached to the session.
	 * 
	 * If an entity to update is already attached to the session, this method
	 * will have no effect. If an entity to update has the same id as another
	 * instance already attached to the session, an error will be thrown.
	 * 
	 */
	void save(AbstractEntity entity);

	/**
	 * Remove the specified entity from the datastore.
	 * 
	 */
	void delete(AbstractEntity entity);
	
	/**
	 * Delete entity of specified class and identifier without actually loading the entity.
	 * @param entityClass
	 * 			class of the entity
	 * @param entityId
	 * 			identifier of the entity
	 */
	<T extends AbstractEntity> void deleteById(Class<T> entityClass, Long entityId);

	/**
	 * Search with specified Hibernate {@link DetachedCriteria}
	 * @param criteria
	 * 			Hibernate {@link DetachedCriteria}
	 * @param firstResult
	 * 			First result of the query. Set to 0 if this value should be ignored.
	 * @param maxResults
	 * 			Max number of returned results. Set to 0 if no limit of the max results should be set. 
	 * @return
	 * 			A list of execution result of the detached criteria.
	 */
	List<?> search(DetachedCriteria criteria, int firstResult, int maxResults);

	/**
	 * This method expects to find a single entity with specified Hibernate detached criteria
	 * @param detachedCriteria
	 * 			Hibernate {@link DetachedCriteria}
	 * @return
	 * 			The single entity or <tt>null</tt> if not found
	 * @throws 
	 * 			HibernateException if there is more than one matching result
	 */
	Object find(DetachedCriteria detachedCriteria);
	
	/**
	 * Count entities of specified class matching specified {@link DetachedCriteria} 
	 * @return
	 * 			Number of entities matching specified {@link DetachedCriteria}
	 */
	<T extends AbstractEntity> int count(DetachedCriteria detachedCriteria);
}
