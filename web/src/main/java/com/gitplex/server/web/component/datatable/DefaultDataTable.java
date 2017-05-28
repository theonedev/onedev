package com.gitplex.server.web.component.datatable;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

@SuppressWarnings("serial")
public class DefaultDataTable<T, S> extends DataTable<T, S> {

	public DefaultDataTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);

		addBottomToolbar(new AjaxNavigationToolbar(this) {

			@Override
			protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> table) {
				return new BootstrapAjaxPagingNavigator(navigatorId, table);
			}
			
		});
		addTopToolbar(new AjaxFallbackHeadersToolbar<S>(this, dataProvider));
		addBottomToolbar(new NoRecordsToolbar(this));
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
		return new OddEvenItem<T>(id, index, model);
	}

}
