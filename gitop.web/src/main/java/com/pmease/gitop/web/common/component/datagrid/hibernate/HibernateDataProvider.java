package com.pmease.gitop.web.common.component.datagrid.hibernate;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.google.common.base.Splitter;
import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.commons.hibernate.dao.GeneralDao;
import com.pmease.commons.loader.AppLoader;
import com.pmease.gitop.web.model.EntityModel;

public abstract class HibernateDataProvider<T extends AbstractEntity> extends SortableDataProvider<T, String> {
	private static final long serialVersionUID = 9007657871051510330L;

	abstract protected DetachedCriteria getCriteria();
	
	protected GeneralDao getDao() {
		return AppLoader.getInstance(GeneralDao.class);
	}
	
	@Override
	public Iterator<? extends T> iterator(long first, long count) {
		GeneralDao dao = getDao();
		DetachedCriteria criteria = getCriteria();
		
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
		
		@SuppressWarnings("unchecked")
		List<T> list = (List<T>) dao.query(criteria, (int) first, (int) count);
		
		return list.iterator();
	}

	@Override
	public long size() {
		GeneralDao dao = getDao();
		return dao.count(getCriteria());
	}

	@Override
	public IModel<T> model(T object) {
		return new EntityModel<T>(object);
	}
}
