package io.onedev.server.web.util.model;

import javax.annotation.Nullable;

import org.apache.wicket.model.LoadableDetachableModel;

import io.onedev.launcher.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.utils.ClassUtils;

public class EntityModel<T extends AbstractEntity> extends LoadableDetachableModel<T> {

	private static final long serialVersionUID = 1L;

	private Class<T> entityClass;

	private Long entityId;
	
	private T entity;
	
	public EntityModel(@Nullable T entity) {
		setObject(entity);
	}

	@Override
	protected T load() {
		if (entityClass != null && entityId != null) {
			return AppLoader.getInstance(Dao.class).load(entityClass, entityId);
		} else {
			return entity;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setObject(@Nullable T object) {
		super.setObject(object);

		if (object != null) {
			entityClass = (Class<T>) ClassUtils.unproxy(object.getClass());
			entityId = object.getId();
		} else {
			entityClass = null;
			entityId = null;
		}

		if (entityId == null)
			entity = object;
		else
			entity = null;
	}
	
}
