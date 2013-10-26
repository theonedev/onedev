package com.pmease.gitop.web.model;

import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Preconditions;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AppLoader;

public class EntityModel<T extends AbstractEntity> extends LoadableDetachableModel<T> {

	private static final long serialVersionUID = 1L;

	private transient T entity;
	private Long id;
	private final Class<T> entityClass;

	protected GeneralDao getDao() {
		return AppLoader.getInstance(GeneralDao.class);
	}

	@SuppressWarnings("unchecked")
	public EntityModel(T entity) {
		Preconditions.checkNotNull(entity, "entity");
		
		if (entity.isNew()) {
			this.entity = entity;
		} else {
			this.id = entity.getId();
		}
		
		this.entityClass = (Class<T>) entity.getClass();
	}

	@Override
	protected T load() {
		if (id != null) {
			return getDao().get(entityClass, id);
		} else {
			return entity;
		}
	}

}
