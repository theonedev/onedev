package com.pmease.commons.persistence.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pmease.commons.persistence.AbstractEntity;
import com.pmease.commons.persistence.Transactional;

@Singleton
@SuppressWarnings("unchecked")
public class DefaultGeneralDao implements GeneralDao {

	private final Provider<SessionFactory> sessionFactoryProvider;
	
	private final Provider<Session> sessionProvider;
	
	@Inject
	public DefaultGeneralDao(Provider<SessionFactory> sessionFactoryProvider, Provider<Session> sessionProvider) {
		this.sessionFactoryProvider = sessionFactoryProvider;
		this.sessionProvider = sessionProvider;
	}
	
	@Transactional
	@Override
	public <T extends AbstractEntity> T lookup(Class<T> entityClass, Long entityId) {
		return (T) getSession().get(unproxy(entityClass), entityId);
	}

	@Transactional
	@Override
	public <T extends AbstractEntity> T load(Class<T> entityClass, Long entityId) {
		return (T) getSession().load(unproxy(entityClass), entityId);
	}

	@Transactional
	@Override
	public void save(AbstractEntity entity) {
		getSession().saveOrUpdate(entity);
	}

	@Transactional
	@Override
	public void delete(AbstractEntity entity) {
		getSession().delete(entity);
	}

	protected Session getSession() {
		return sessionProvider.get();
	}

	@Transactional
	@Override
	public <T extends AbstractEntity> void deleteById(Class<T> entityClass, Long entityId) {
		T entity = load(unproxy(entityClass), entityId);
		delete(entity);
	}
	
	protected <T extends AbstractEntity> Class<T> unproxy(Class<T> entityClass) {
		//cm will be null if entityClass is not registered with Hibernate or when
		//it is a Hibernate proxy class (e.x. test.googlecode.genericdao.model.Person_$$_javassist_5).
		//So if a class is not recognized, we will look at superclasses to see if
		//it is a proxy.
		while (sessionFactoryProvider.get().getClassMetadata(entityClass) == null) {
			entityClass = (Class<T>) entityClass.getSuperclass();
			if (Object.class.equals(entityClass))
				return null;
		}
		
		return (Class<T>) entityClass;
	}

	@Transactional
	@Override
	public List<?> search(DetachedCriteria detachedCriteria, int firstResult, int maxResults) {
		Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
		if (firstResult != 0)
			criteria.setFirstResult(firstResult);
		if (maxResults != 0)
			criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Transactional
	@Override
	public Object lookup(DetachedCriteria detachedCriteria) {
		Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
		return criteria.uniqueResult();
	}

	@Override
	public <T extends AbstractEntity> int count(DetachedCriteria detachedCriteria) {
		Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
		criteria.setProjection(Projections.rowCount());
		return (Integer) criteria.uniqueResult();
	}
	
}
