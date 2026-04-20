package io.onedev.server.web.editable.workspacespec.envvar;

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

import io.onedev.server.model.support.workspace.spec.EnvVar;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class EnvVarListViewPanel extends DrawCardBeanListViewPanel<EnvVar> {

	private static final long serialVersionUID = 1L;

	EnvVarListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(EnvVar item) {
		return item.getName();
	}

	@Override
	protected List<IColumn<EnvVar, Void>> getDataColumns() {
		List<IColumn<EnvVar, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<EnvVar, Void>(Model.of(_T("Name"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvVar>> cellItem, String componentId, IModel<EnvVar> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}

		});

		columns.add(new AbstractColumn<EnvVar, Void>(Model.of(_T("Value"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvVar>> cellItem, String componentId, IModel<EnvVar> rowModel) {
				if (rowModel.getObject().isSecret())
					cellItem.add(new Label(componentId, "********"));
				else
					cellItem.add(new Label(componentId, rowModel.getObject().getValue()));
			}

		});

		return columns;
	}

}
