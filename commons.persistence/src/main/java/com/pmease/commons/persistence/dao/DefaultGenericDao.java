package com.pmease.commons.persistence.dao;

import java.util.List;

import javax.inject.Provider;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.google.inject.Inject;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.commons.util.ClassUtils;

public class DefaultGenericDao<T extends AbstractEntity> implements GenericDao<T> {

	private GeneralDao generalDao;
	
	private Provider<Session> sessionProvider;
	
	protected final Class<T> entityClass;

	@SuppressWarnings("unchecked")
	@Inject
	public DefaultGenericDao(GeneralDao generalDao, Provider<Session> sessionProvider) {
		this.generalDao = generalDao;
		this.sessionProvider = sessionProvider;
		List<Class<?>> typeArguments = ClassUtils.getTypeArguments(DefaultGenericDao.class, getClass());
		entityClass = ((Class<T>) typeArguments.get(0));
	}
	
	@Override
	public T find(Long entityId) {
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
	
	@Override
	public List<T> search(Criterion[] criterions) {
		return search(criterions, null, 0, 0);
	}

	@Override
	public Object find(Criterion[] criterions) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
		
		if (criterions != null) {
			for (Criterion criterion: criterions)
				detachedCriteria.add(criterion);
		}
		
		return generalDao.find(detachedCriteria);
	}

	protected Session getSession() {
		return sessionProvider.get();
	}
}
