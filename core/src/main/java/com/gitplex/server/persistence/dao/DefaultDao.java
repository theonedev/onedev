package com.gitplex.server.persistence.dao;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;

import com.gitplex.launcher.loader.ListenerRegistry;
import com.gitplex.launcher.loader.ManagedSerializedForm;
import com.gitplex.server.model.AbstractEntity;
import com.gitplex.server.persistence.UnitOfWork;
import com.gitplex.server.persistence.annotation.Sessional;
import com.gitplex.server.persistence.annotation.Transactional;
import com.gitplex.server.util.ClassUtils;

@Singleton
@SuppressWarnings("unchecked")
public class DefaultDao implements Dao, Serializable {

	private final UnitOfWork unitOfWork;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultDao(UnitOfWork unitOfWork, ListenerRegistry listenerRegistry) {
		this.unitOfWork = unitOfWork;
		this.listenerRegistry = listenerRegistry;
	}
	
	@Sessional
	@Override
	public <T extends AbstractEntity> T get(Class<T> entityClass, Long entityId) {
		return (T) getSession().get(ClassUtils.unproxy(entityClass), entityId);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> T load(Class<T> entityClass, Long entityId) {
		return (T) getSession().load(ClassUtils.unproxy(entityClass), entityId);
	}

	@Transactional
	@Override
	public void persist(AbstractEntity entity) {
		boolean isNew = entity.isNew();
		getSession().saveOrUpdate(entity);
		listenerRegistry.post(new EntityPersisted(entity, isNew));
	}

	@Transactional
	@Override
	public void remove(AbstractEntity entity) {
		getSession().delete(entity);
		listenerRegistry.post(new EntityRemoved(entity));
	}

	@Override
	public Session getSession() {
		return unitOfWork.getSession();
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> findRange(EntityCriteria<T> entityCriteria, int firstResult, int maxResults) {
		Criteria criteria = entityCriteria.getExecutableCriteria(getSession());
		criteria.setFirstResult(firstResult);
		criteria.setMaxResults(maxResults);
		return criteria.list();
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> findAll(EntityCriteria<T> entityCriteria) {
		return findRange(entityCriteria, 0, Integer.MAX_VALUE);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> T find(EntityCriteria<T> entityCriteria) {
		Criteria criteria = entityCriteria.getExecutableCriteria(getSession());
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		return (T) criteria.uniqueResult();
	}

	@Override
	public <T extends AbstractEntity> int count(EntityCriteria<T> entityCriteria) {
		Criteria criteria = entityCriteria.getExecutableCriteria(getSession());
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(Dao.class);
	}

	@Sessional
	@Override
	public <T extends AbstractEntity> List<T> findAll(Class<T> entityClass) {
		return findRange(EntityCriteria.of(entityClass), 0, Integer.MAX_VALUE);
	}

	@Override
	public void doAfterCommit(Runnable runnable) {
		if (getSession().getTransaction().isActive()) {
			getSession().getTransaction().registerSynchronization(new Synchronization() {
				
				@Override
				public void beforeCompletion() {
				}
				
				@Override
				public void afterCompletion(int status) {
					if (status == Status.STATUS_COMMITTED)
						runnable.run();
				}
				
			});
		} else {
			runnable.run();
		}
	}	
	
	@Override
	public void doUnitOfWorkAsyncAfterCommit(Runnable runnable) {
		doAfterCommit(new Runnable() {

			@Override
			public void run() {
				unitOfWork.doAsync(runnable);
			}
			
		});
	}	

}
