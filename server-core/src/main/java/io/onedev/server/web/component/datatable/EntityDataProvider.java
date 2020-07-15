package io.onedev.server.web.component.datatable;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.hibernate.criterion.Order;

import io.onedev.commons.launcher.loader.AppLoader;
import io.onedev.server.model.AbstractEntity;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;
import io.onedev.server.web.util.LoadableDetachableDataProvider;

@SuppressWarnings("serial")
public class EntityDataProvider<T extends AbstractEntity> extends LoadableDetachableDataProvider<T, String> {
	
	private final Class<T> entityType;
	
	public EntityDataProvider(Class<T> entityType, SortParam<String> initialSort) {
		this.entityType = entityType;
		
		if (initialSort != null)
			setSort(initialSort);
	}

	@Override
	public Iterator<T> iterator(long first, long count) {
		EntityCriteria<T> criteria = EntityCriteria.of(entityType);
		
		if (getSort() != null) {
			if (getSort().isAscending())
				criteria.addOrder(Order.asc(getSort().getProperty()));
			else
				criteria.addOrder(Order.desc(getSort().getProperty()));
		} 
		
		return AppLoader.getInstance(Dao.class).query(criteria, (int)first, (int)count).iterator();
	}

	@Override
	public long calcSize() {
		EntityCriteria<? extends AbstractEntity> criteria = EntityCriteria.of(entityType);
		return AppLoader.getInstance(Dao.class).count(criteria);
	}

	@Override
	public IModel<T> model(AbstractEntity entity) {
		Long entityId = entity.getId();
		return new LoadableDetachableModel<T>() {

			@Override
			protected T load() {
				return AppLoader.getInstance(Dao.class).load(entityType, entityId);
			}
			
		};
	}

}
