package io.onedev.server.web.component.datatable;

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;

public class DefaultDataTable<T, S> extends DataTable<T, S> {

	public DefaultDataTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage, 
			@Nullable PagingHistorySupport pagingHistorySupport) {
		super(id, columns, dataProvider, rowsPerPage);

		if (pagingHistorySupport != null)
			setCurrentPage(pagingHistorySupport.getCurrentPage());
		
		addTopToolbar(new AjaxFallbackHeadersToolbar<S>(this, dataProvider));
		
		addBottomToolbar(new NavigationToolbar(this) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new OnePagingNavigator(navigatorId, table, pagingHistorySupport);
			}

		});
		addBottomToolbar(new NoRecordsToolbar(this, Model.of("Unspecified")));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new NoRecordsBehavior());
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
		return new OddEvenItem<T>(id, index, model);
	}

}
