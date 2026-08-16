package io.onedev.server.web.component.datatable;

import static io.onedev.server.web.translation.Translation._T;

import java.text.MessageFormat;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.jspecify.annotations.Nullable;

import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.util.paginghistory.PagingHistorySupport;

public class ListNavigationToolbar extends AbstractToolbar {

	public ListNavigationToolbar(DataTable<?, ?> table, @Nullable PagingHistorySupport pagingHistorySupport) {
		super(table);

		var span = new WebMarkupContainer("span");
		span.add(AttributeModifier.replace("colspan", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return String.valueOf(table.getColumns().size());
			}

		}));
		span.add(new Label("count", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return MessageFormat.format(_T("{0} found"), String.valueOf(table.getItemCount()));
			}

		}));
		span.add(new OnePagingNavigator("navigator", table, pagingHistorySupport));
		add(span);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();
		setVisible(getTable().getPageCount() > 1);
	}

}
