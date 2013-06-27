package com.pmease.commons.persistence.dao;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;


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
	public T find(Long entityId) {
		return generalDao.find(entityClass, entityId);
	}

	@Override
	public T getReference(Long entityId) {
		return generalDao.getReference(entityClass, entityId);
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
	public List<T> search(Criterion[] criterions, Order[] orders, int firstResult, int maxResults) {
		return generalDao.search(entityClass, criterions, orders, firstResult, maxResults);
	}

	@Override
	public int count(Criterion[] criterions) {
		return generalDao.count(entityClass, criterions);
	}

}
