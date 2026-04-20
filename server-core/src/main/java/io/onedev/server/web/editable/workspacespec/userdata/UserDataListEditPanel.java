package io.onedev.server.web.editable.workspacespec.userdata;

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

import io.onedev.server.model.support.workspace.spec.UserData;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;

class UserDataListEditPanel extends DrawCardBeanListEditPanel<UserData> {

	private static final long serialVersionUID = 1L;

	UserDataListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new user data");
	}

	@Override
	protected DrawCardBeanItemEditPanel<UserData> newEditPanel(String id, List<UserData> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new UserDataEditPanel(id, items, index, callback);
	}

	@Override
	protected List<IColumn<UserData, Void>> getDataColumns() {
		List<IColumn<UserData, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<UserData, Void>(Model.of(_T("Key"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<UserData>> cellItem, String componentId, IModel<UserData> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getKey()));
			}

		});

		columns.add(new AbstractColumn<UserData, Void>(Model.of(_T("#Paths"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<UserData>> cellItem, String componentId, IModel<UserData> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getPaths().size()));
			}

		});

		return columns;
	}

}
