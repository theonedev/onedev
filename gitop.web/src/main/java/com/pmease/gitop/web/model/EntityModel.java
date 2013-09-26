package com.pmease.gitop.web.model;


import org.apache.wicket.model.LoadableDetachableModel;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.GenericDao;

public abstract class EntityModel<T extends AbstractEntity> extends LoadableDetachableModel<T> {

  private static final long serialVersionUID = 1L;
  
  protected T entity;
  
  abstract protected GenericDao<T> getDao();
  
  public EntityModel(T entity) {
    this.entity = entity;
  }
  
  @Override
  protected T load() {
    if (entity.isNew()) {
      return entity;
    } else {
      return getDao().get(entity.getId());
    }
  }

}
