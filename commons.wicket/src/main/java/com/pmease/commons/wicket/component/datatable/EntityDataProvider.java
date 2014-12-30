package com.pmease.commons.wicket.component.datatable;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Order;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.AppLoader;

@SuppressWarnings("serial")
public class EntityDataProvider<T extends AbstractEntity> extends SortableDataProvider<T, String> {
	
	private final Class<T> entityType;
	
	public EntityDataProvider(Class<T> entityType, SortParam<String> initialSort) {
		this.entityType = entityType;
		
		if (initialSort != null)
			setSort(initialSort);
	}

	@Override
	public Iterator<T> iterator(long first, long count) {
		EntityCriteria<T> criteria = EntityCriteria.of(entityType);
		restrict(criteria);
		
		if (getSort() != null) {
			if (getSort().isAscending())
				criteria.addOrder(Order.asc(getSort().getProperty()));
			else
				criteria.addOrder(Order.desc(getSort().getProperty()));
		} 
		
		return AppLoader.getInstance(Dao.class).query(criteria, (int)first, (int)count).iterator();
	}

	@Override
	public long size() {
		EntityCriteria<? extends AbstractEntity> criteria = EntityCriteria.of(entityType);
		restrict(criteria);
		return AppLoader.getInstance(Dao.class).count(criteria);
	}

	@Override
	public IModel<T> model(AbstractEntity entity) {
		final Long entityId = entity.getId();
		return new LoadableDetachableModel<T>() {

			@Override
			protected T load() {
				return AppLoader.getInstance(Dao.class).load(entityType, entityId);
			}
			
		};
	}

	protected void restrict(EntityCriteria<? extends AbstractEntity> criteria) {
		
	}
	
}
