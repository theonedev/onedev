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
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class UserDataListViewPanel extends DrawCardBeanListViewPanel<UserData> {

	private static final long serialVersionUID = 1L;

	UserDataListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(UserData item) {
		return item.getKey();
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
