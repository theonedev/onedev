package io.onedev.server.persistence.dao;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.entity.EntityPersisted;
import io.onedev.server.event.entity.EntityRemoved;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.SessionService;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import org.hibernate.query.Query;
import org.hibernate.Session;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

@Singleton
public class DefaultDao implements Dao, Serializable {

	private final SessionService sessionService;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultDao(SessionService sessionService, ListenerRegistry listenerRegistry) {
		this.sessionService = sessionService;
		this.listenerRegistry = listenerRegistry;
	}
	
	@Sessional
	@Override
	public <T extends AbstractEntity> T get(Class<T> entityClass, Long entityId) {
		return getSession().find(entityClass, entityId);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> T load(Class<T> entityClass, Long entityId) {
		return getSession().getReference(entityClass, entityId);
	}

	@Transactional
	@Override
	public void persist(AbstractEntity entity) {
		boolean wasNew = entity.isNew();
		if (wasNew)
			getSession().persist(entity);
		else if (!getSession().contains(entity))
			entity = getSession().merge(entity);
		listenerRegistry.post(new EntityPersisted(entity, wasNew));
	}

	@Transactional
	@Override
	public void remove(AbstractEntity entity) {
		getSession().remove(entity);
		listenerRegistry.post(new EntityRemoved(entity));
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> query(EntityCriteria<T> entityCriteria, int firstResult, int maxResults) {
		Query<T> criteria = entityCriteria.getExecutableCriteria(getSession());
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> query(EntityCriteria<T> entityCriteria) {
		return query(entityCriteria, 0, Integer.MAX_VALUE);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> T find(EntityCriteria<T> entityCriteria) {
		Query<T> criteria = entityCriteria.getExecutableCriteria(getSession());
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (T) criteria.uniqueResult();
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> int count(EntityCriteria<T> entityCriteria) {
		return Math.toIntExact(entityCriteria.count(getSession()));
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(Dao.class);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> query(Class<T> entityClass) {
		return query(entityClass, false);
	}
	
	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> query(Class<T> entityClass, boolean cacheable) {
		EntityCriteria<T> criteria = EntityCriteria.of(entityClass);		
		criteria.setCacheable(cacheable);
		return query(criteria, 0, Integer.MAX_VALUE);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> int count(Class<T> entityClass) {
		return count(entityClass, false);
	}
	
	@Sessional
	@Override
	public <T extends AbstractEntity> int count(Class<T> entityClass, boolean cacheable) {
		EntityCriteria<T> criteria = EntityCriteria.of(entityClass);		
		criteria.setCacheable(cacheable);
		return count(criteria);
	}
	
	@Override
	public SessionService getSessionService() {
		return sessionService;
	}

	@Override
	public Session getSession() {
		return sessionService.getSession();
	}
	
}
