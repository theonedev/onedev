package com.pmease.gitplex.web.common.wicket.component.datagrid;

import java.util.List;

import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;

import com.pmease.gitplex.web.common.wicket.component.datagrid.event.PageChanged;

@SuppressWarnings("serial")
public class DataGrid<T> extends DataTable<T, String>{

	public DataGrid(String id, List<? extends IColumn<T, String>> columns,
			IDataProvider<T> dataProvider, long rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
		
		setOutputMarkupId(true);
		setVersioned(false);
	}

	@Override
	public void onEvent(IEvent<?> event) {
		if (event.getPayload() instanceof PageChanged) {
			PageChanged payload = (PageChanged) event.getPayload();
			setCurrentPage(payload.getPage());
			payload.getTarget().add(this);
		}
	}
}
