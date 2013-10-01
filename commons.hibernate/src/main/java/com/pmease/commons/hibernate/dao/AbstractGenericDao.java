package com.pmease.commons.hibernate.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.google.inject.Inject;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.util.ReflectionUtils;

public abstract class AbstractGenericDao<T extends AbstractEntity> implements GenericDao<T> {

	private GeneralDao generalDao;
	
	protected final Class<T> entityClass;

	@SuppressWarnings("unchecked")
	@Inject
	public AbstractGenericDao(GeneralDao generalDao) {
		this.generalDao = generalDao;
		List<Class<?>> typeArguments = ReflectionUtils.getTypeArguments(AbstractGenericDao.class, getClass());
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
	public List<T> query(Criterion... criterions) {
		return query(criterions, null, 0, 0);
	}

	@Override
	public List<T> query() {
		return query(new Criterion[0]);
	}

	@Override
	public T find(Criterion... criterions) {
		return find(criterions, null);
	}

	@Override
	public T find() {
		return find(new Criterion[0]);
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

	@Override
	public Session getSession() {
		return generalDao.getSession();
	}

	@Override
	public Criteria createCriteria() {
		return getSession().createCriteria(entityClass);
	}
	
}
