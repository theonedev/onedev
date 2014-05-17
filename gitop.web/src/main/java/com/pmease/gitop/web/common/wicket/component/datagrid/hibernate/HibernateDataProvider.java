package com.pmease.gitop.web.common.wicket.component.datagrid.hibernate;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.hibernate.criterion.Order;

import com.google.common.base.Splitter;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.commons.hibernate.dao.EntityCriteria;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.web.model.EntityModel;

public abstract class HibernateDataProvider<T extends AbstractEntity> extends SortableDataProvider<T, String> {
	private static final long serialVersionUID = 9007657871051510330L;

	abstract protected EntityCriteria<T> getCriteria();
	
	protected Dao getDao() {
		return AppLoader.getInstance(Dao.class);
	}
	
	@Override
	public Iterator<? extends T> iterator(long first, long count) {
		Dao dao = getDao();
		EntityCriteria<T> criteria = getCriteria();
		
		SortParam<String> param = getSort();
		if (param != null) {
			Iterable<String> it = Splitter.on(",").trimResults().split(param.getProperty());
			for (String each : it) {
				if (param.isAscending())
					criteria.addOrder(Order.asc(each));
				else
					criteria.addOrder(Order.desc(each));
			}
		}
		
		List<T> list = (List<T>) dao.query(criteria, (int) first, (int) count);
		
		return list.iterator();
	}

	@Override
	public long size() {
		Dao dao = getDao();
		return dao.count(getCriteria());
	}

	@Override
	public IModel<T> model(T object) {
		return new EntityModel<T>(object);
	}
}
