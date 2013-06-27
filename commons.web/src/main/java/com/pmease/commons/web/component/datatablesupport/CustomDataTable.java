package com.pmease.commons.web.component.datatablesupport;

import java.util.List;

import org.apache.wicket.bootstrap.Bootstrap;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class CustomDataTable<T, S> extends DataTable<T, S> {

	public CustomDataTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);

		addBottomToolbar(new CustomAjaxNavigationToolbar(this));
		addTopToolbar(new CustomAjaxHeadersToolbar<S>(this, dataProvider));
		addBottomToolbar(new NoRecordsToolbar(this));
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
		return new OddEvenItem<T>(id, index, model);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(Bootstrap
				.responsive()));
	}

}
