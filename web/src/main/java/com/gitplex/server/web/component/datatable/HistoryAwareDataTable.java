package com.gitplex.server.web.component.datatable;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

import com.gitplex.server.web.util.PagingHistorySupport;

@SuppressWarnings("serial")
public class HistoryAwareDataTable<T, S> extends DataTable<T, S> {

	public HistoryAwareDataTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage, 
			@Nullable PagingHistorySupport pagingHistorySupport) {
		super(id, columns, dataProvider, rowsPerPage);

		if (pagingHistorySupport != null)
			setCurrentPage(pagingHistorySupport.getCurrentPage());
		addBottomToolbar(new HistoryAwareNavToolbar(this, pagingHistorySupport));
		addTopToolbar(new AjaxFallbackHeadersToolbar<S>(this, dataProvider));
		addBottomToolbar(new NoRecordsToolbar(this));
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
		return new OddEvenItem<T>(id, index, model);
	}

}
