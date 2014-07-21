package com.pmease.gitplex.web.common.wicket.component.datagrid.hibernate;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.WildcardListModel;

import com.pmease.commons.hibernate.AbstractEntity;
import com.pmease.gitplex.web.model.EntityModel;

@SuppressWarnings("serial")
public class EntityListProvider<T extends AbstractEntity> extends ListDataProvider<T> implements ISortableDataProvider<T, String> {
	
	final IModel<List<T>> listModel;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EntityListProvider(List<T> list) {
		this.listModel = new WildcardListModel(list);
	}
	
	public EntityListProvider(IModel<List<T>> model) {
		this.listModel = model;
	}
	
	@Override
	public IModel<T> model(T object) {
		return new EntityModel<T>(object);
	}

	@Override
	protected List<T> getData() {
		return listModel.getObject();
	}
	
	@Override
	public void detach() {
		if (listModel != null) {
			listModel.detach();
		}
		
		super.detach();
	}

	@Override
	public ISortState<String> getSortState() {
		return null;
	}

}
