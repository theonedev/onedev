package com.gitplex.server.web.component.datatable;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

import com.gitplex.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class HistoryAwareNavToolbar extends NavigationToolbar {

	private final PagingHistorySupport pagingHistorySupport;
	
	public HistoryAwareNavToolbar(DataTable<?, ?> table, PagingHistorySupport pagingHistorySupport) {
		super(table);
		this.pagingHistorySupport = pagingHistorySupport;
	}
	
	@Override
	protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
		return new HistoryAwarePagingNavigator(navigatorId, table, pagingHistorySupport);
	}

}
