package com.pmease.commons.hibernate.dao;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.Sessional;
import com.pmease.commons.hibernate.Transactional;
import com.pmease.commons.loader.ManagedSerializedForm;

@Singleton
@SuppressWarnings("unchecked")
public class DefaultGeneralDao implements GeneralDao, Serializable {

	private final Provider<SessionFactory> sessionFactoryProvider;
	
	private final Provider<Session> sessionProvider;
	
	@Inject
	public DefaultGeneralDao(Provider<SessionFactory> sessionFactoryProvider, 
			Provider<Session> sessionProvider) {
		this.sessionFactoryProvider = sessionFactoryProvider;
		this.sessionProvider = sessionProvider;
	}
	
	@Sessional
	@Override
	public <T extends AbstractEntity> T get(Class<T> entityClass, Long entityId) {
		return (T) getSession().get(unproxy(entityClass), entityId);
	}

	@Sessional
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

	@Override
	public Session getSession() {
		return sessionProvider.get();
	}

	protected <T extends AbstractEntity> Class<T> unproxy(Class<T> entityClass) {
		//class meta data will be null if entityClass is not registered with Hibernate or when
		//it is a Hibernate proxy class (e.x. test.googlecode.genericdao.model.Person_$$_javassist_5).
		//So if a class is not recognized, we will look at superclasses to see if
		//it is a proxy.
		while (sessionFactoryProvider.get().getClassMetadata(entityClass) == null) {
			entityClass = (Class<T>) entityClass.getSuperclass();
			if (Object.class.equals(entityClass))
				return null;
		}
		
		return entityClass;
	}

	@Sessional
	@Override
	public List<?> query(DetachedCriteria detachedCriteria, int firstResult, int maxResults) {
		Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
		if (firstResult != 0)
			criteria.setFirstResult(firstResult);
		if (maxResults != 0)
			criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Sessional
	@Override
	public Object find(DetachedCriteria detachedCriteria) {
		Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return criteria.uniqueResult();
	}

	@Override
	public <T extends AbstractEntity> int count(DetachedCriteria detachedCriteria) {
		Criteria criteria = detachedCriteria.getExecutableCriteria(getSession());
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(GeneralDao.class);
	}	

}
