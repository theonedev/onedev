package com.pmease.gitop.web.model;

import javax.annotation.Nullable;

import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.JavassistUtils;

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
			entityClass = (Class<T>) JavassistUtils.unproxy(object.getClass());
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
