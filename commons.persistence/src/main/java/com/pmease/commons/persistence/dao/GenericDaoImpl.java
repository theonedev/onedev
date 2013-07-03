package com.pmease.commons.persistence.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.commons.util.ClassUtils;

public class GenericDaoImpl<T extends AbstractEntity> implements GenericDao<T> {

	private GeneralDao generalDao;
	
	protected final Class<T> entityClass;

	@SuppressWarnings("unchecked")
	@Inject
	public GenericDaoImpl(GeneralDao generalDao) {
		this.generalDao = generalDao;
		List<Class<?>> typeArguments = ClassUtils.getTypeArguments(GenericDaoImpl.class, getClass());
		entityClass = ((Class<T>) typeArguments.get(0));
	}
	
	@Override
	public Optional<T> find(Long entityId) {
		return generalDao.find(entityClass, entityId);
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
	public List<T> search(Criterion[] criterions, Order[] orders, int firstResult, int maxResults) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
		
		if (criterions != null) {
			for (Criterion criterion: criterions)
				detachedCriteria.add(criterion);
		}
		
		if (orders != null) {
			for (Order order: orders)
				detachedCriteria.addOrder(order);
		}
		
		return (List<T>) generalDao.search(detachedCriteria, firstResult, maxResults);
	}

}
