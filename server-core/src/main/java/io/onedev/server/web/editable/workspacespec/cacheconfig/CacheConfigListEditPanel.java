package io.onedev.server.web.editable.workspacespec.cacheconfig;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.workspace.spec.CacheConfig;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.util.TextUtils;

class CacheConfigListEditPanel extends DrawCardBeanListEditPanel<CacheConfig> {

	private static final long serialVersionUID = 1L;

	CacheConfigListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new cache config");
	}

	@Override
	protected DrawCardBeanItemEditPanel<CacheConfig> newEditPanel(String id, List<CacheConfig> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new CacheConfigEditPanel(id, items, index, callback);
	}

	@Override
	protected List<IColumn<CacheConfig, Void>> getDataColumns() {
		List<IColumn<CacheConfig, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of(_T("Cache Key"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getKey()));
			}

		});

		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of(_T("#Paths"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getPaths().size()));
			}

		});

		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of(_T("Upload Strategy"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				cellItem.add(new Label(componentId, TextUtils.getDisplayValue(rowModel.getObject().getUploadStrategy())));
			}

		});

		return columns;
	}

}
