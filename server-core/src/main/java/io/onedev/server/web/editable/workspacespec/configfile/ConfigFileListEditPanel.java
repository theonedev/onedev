package io.onedev.server.web.editable.workspacespec.configfile;

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

import io.onedev.server.model.support.workspace.spec.ConfigFile;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;

class ConfigFileListEditPanel extends DrawCardBeanListEditPanel<ConfigFile> {

	private static final long serialVersionUID = 1L;

	ConfigFileListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new config file");
	}

	@Override
	protected DrawCardBeanItemEditPanel<ConfigFile> newEditPanel(String id, List<ConfigFile> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new ConfigFileEditPanel(id, items, index, callback);
	}

	@Override
	protected List<IColumn<ConfigFile, Void>> getDataColumns() {
		List<IColumn<ConfigFile, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<ConfigFile, Void>(Model.of(_T("Path"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ConfigFile>> cellItem, String componentId, IModel<ConfigFile> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getPath()));
			}

		});

		return columns;
	}

}
