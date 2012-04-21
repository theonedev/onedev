package com.pmease.commons.hibernate.dao;

import java.util.List;

import javax.annotation.Nullable;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.pmease.commons.hibernate.AbstractEntity;

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
	<T extends AbstractEntity> T getReference(Class<T> entityClass, Long entityId);

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
	 * Search with specified Hibernate detached criteria. 
	 * @param criteria
	 * 			Hibernate detached criteria, refer to hibernate documentation for details.
	 * @param firstResult
	 * 			First result of the query. Set to 0 if this value should be ignored.
	 * @param maxResults
	 * 			Max number of returned results. Set to 0 if no limit of the max results should be set. 
	 * @return
	 * 			A list of execution result of the detached criteria.
	 */
	List<?> search(DetachedCriteria criteria, int firstResult, int maxResults);

	/**
	 * Search specified entity with specified hibernate criterions and orders. 
	 * @param entityClass
	 * 			Class of the entity to search.
	 * @param criterions
	 * 			Hibernate criterions to restrict search result. No restrictions will be set 
	 * 			if pass a null value or empty array.
	 * @param orders
	 * 			Hibernate orders to order search result. No orders will be set if pass a 
	 * 			null value or empty array.
	 * @param firstResult
	 * 			First result of the query. Set to 0 if this value should be ignored.
	 * @param maxResults
	 * 			Max number of returned entities. Set to 0 if no limit of number of returned 
	 * 			entities should be set.
	 * @return
	 * 			List of matched entities.
	 */
	<T extends AbstractEntity> List<T> search(Class<T> entityClass, @Nullable Criterion[] criterions, 
			@Nullable Order[] orders, int firstResult, int maxResults);
	
	/**
	 * Count entities of specified class matching specified hibernate criterions. 
	 * @param entityClass
	 * 			Class of entity to count.
	 * @param criterias
	 * 			Hibernate criterions to restrict entities to be counted. No retrictions will be 
	 * 			set if pass a null value or empty array.
	 * @return
	 * 			Number of entities matching specified criterions.
	 */
	<T extends AbstractEntity> int count(Class<T> entityClass, @Nullable Criterion[] criterias);
}
