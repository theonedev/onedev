package com.pmease.gitop.web.model;

import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AppLoader;

public class EntityModel<T extends AbstractEntity> extends LoadableDetachableModel<T> {

	private static final long serialVersionUID = 1L;

	protected T entity;
	private final Class<T> entityClass;

	protected GeneralDao getDao() {
		return AppLoader.getInstance(GeneralDao.class);
	}

	@SuppressWarnings("unchecked")
	public EntityModel(T entity) {
		Preconditions.checkNotNull(entity, "entity");
		this.entity = entity;
		this.entityClass = (Class<T>) entity.getClass();
	}

	@Override
	protected T load() {
		if (entity.isNew()) {
			return entity;
		} else {
			return getDao().get(entityClass, entity.getId());
		}
	}

}
