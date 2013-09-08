package com.pmease.commons.persistence.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.google.inject.Inject;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.commons.util.ClassUtils;

public class DefaultGenericDao<T extends AbstractEntity> implements GenericDao<T> {

	private GeneralDao generalDao;
	
	protected final Class<T> entityClass;

	@SuppressWarnings("unchecked")
	@Inject
	public DefaultGenericDao(GeneralDao generalDao) {
		this.generalDao = generalDao;
		List<Class<?>> typeArguments = ClassUtils.getTypeArguments(DefaultGenericDao.class, getClass());
		entityClass = ((Class<T>) typeArguments.get(0));
	}
	
	@Override
	public T get(Long entityId) {
		return generalDao.get(entityClass, entityId);
	}

	@Override
	public T load(Long entityId) {
		return generalDao.load(entityClass, entityId);
	}

	@Override
	public void save(T entity) {
		generalDao.save(entity);
	}

	@Override
	public void delete(T entity) {
		generalDao.delete(entity);
	}

	@Override
	public void deleteById(Long entityId) {
		generalDao.deleteById(entityClass, entityId);
	}

	@Override
	public int count(Criterion[] criterions) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
		if (criterions != null) {
			for (Criterion criterion: criterions)
				detachedCriteria.add(criterion);
		}
		return generalDao.count(detachedCriteria);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> query(Criterion[] criterions, Order[] orders, int firstResult, int maxResults) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
		
		if (criterions != null) {
			for (Criterion criterion: criterions)
				detachedCriteria.add(criterion);
		}
		
		if (orders != null) {
			for (Order order: orders)
				detachedCriteria.addOrder(order);
		}
		
		return (List<T>) generalDao.query(detachedCriteria, firstResult, maxResults);
	}
	
	@Override
	public List<T> query(Criterion[] criterions) {
		return query(criterions, null, 0, 0);
	}

	@Override
	public T find(Criterion[] criterions) {
		return find(criterions, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T find(Criterion[] criterions, Order[] orders) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
		
		if (criterions != null) {
			for (Criterion criterion: criterions)
				detachedCriteria.add(criterion);
		}
		
		if (orders != null) {
			for (Order order: orders)
				detachedCriteria.addOrder(order);
		}
		
		return (T) generalDao.find(detachedCriteria);
	}

}
