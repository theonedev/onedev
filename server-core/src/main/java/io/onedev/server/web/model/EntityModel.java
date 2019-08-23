package io.onedev.server.web.model;

import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.proxy.HibernateProxyHelper;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;

public class EntityModel<T extends AbstractEntity> extends LoadableDetachableModel<T> {

	private static final long serialVersionUID = 1L;

	private final Class<T> entityClass;

	private final Long entityId;
	
	public EntityModel(Class<T> entityClass, Long entityId) {
		this.entityClass = entityClass;
		this.entityId = entityId;
	}
	
	@SuppressWarnings("unchecked")
	public EntityModel(T entity) {
		this((Class<T>) HibernateProxyHelper.getClassWithoutInitializingProxy(entity), entity.getId());
		setObject(entity);
	}

	@Override
	protected T load() {
		return AppLoader.getInstance(Dao.class).load(entityClass, entityId);
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}
	
	public Long getEntityId() {
		return entityId;
	}
	
}
