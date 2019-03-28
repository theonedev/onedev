package io.onedev.server.persistence.dao;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;

import io.onedev.commons.launcher.loader.ListenerRegistry;
import io.onedev.commons.launcher.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ClassUtils;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;

@Singleton
@SuppressWarnings("unchecked")
public class DefaultDao implements Dao, Serializable {

	private final SessionManager sessionManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultDao(SessionManager sessionManager, ListenerRegistry listenerRegistry) {
		this.sessionManager = sessionManager;
		this.listenerRegistry = listenerRegistry;
	}
	
	@Sessional
	@Override
	public <T extends AbstractEntity> T get(Class<T> entityClass, Long entityId) {
		return (T) sessionManager.getSession().get(ClassUtils.unproxy(entityClass), entityId);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> T load(Class<T> entityClass, Long entityId) {
		return (T) sessionManager.getSession().load(ClassUtils.unproxy(entityClass), entityId);
	}

	@Transactional
	@Override
	public void persist(AbstractEntity entity) {
		boolean isNew = entity.isNew();
		sessionManager.getSession().saveOrUpdate(entity);
		listenerRegistry.post(new EntityPersisted(entity, isNew));
	}

	@Transactional
	@Override
	public void remove(AbstractEntity entity) {
		sessionManager.getSession().delete(entity);
		listenerRegistry.post(new EntityRemoved(entity));
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> query(EntityCriteria<T> entityCriteria, int firstResult, int maxResults) {
		Criteria criteria = entityCriteria.getExecutableCriteria(sessionManager.getSession());
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> query(EntityCriteria<T> entityCriteria) {
		return query(entityCriteria, 0, Integer.MAX_VALUE);
	}

	@Transactional
	@Override
	public <T extends AbstractEntity> T find(EntityCriteria<T> entityCriteria) {
		Criteria criteria = entityCriteria.getExecutableCriteria(sessionManager.getSession());
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (T) criteria.uniqueResult();
	}

	@Override
	public <T extends AbstractEntity> int count(EntityCriteria<T> entityCriteria) {
		Criteria criteria = entityCriteria.getExecutableCriteria(sessionManager.getSession());
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(Dao.class);
	}

	@Transactional
	@Override
	public <T extends AbstractEntity> List<T> query(Class<T> entityClass) {
		return query(EntityCriteria.of(entityClass), 0, Integer.MAX_VALUE);
	}

	@Override
	public SessionManager getSessionManager() {
		return sessionManager;
	}

}
