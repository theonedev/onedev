package io.onedev.server.web.editable.buildspec.job.postbuildaction;

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

import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class PostBuildActionListViewPanel extends DrawCardBeanListViewPanel<PostBuildAction> {

	private static final long serialVersionUID = 1L;

	PostBuildActionListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(PostBuildAction item) {
		String actionType = EditableUtils.getDisplayName(item.getClass());
		return _T("Post Build Action") + " (" + _T("type") + ": " + actionType + ")";
	}

	@Override
	protected List<IColumn<PostBuildAction, Void>> getDataColumns() {
		List<IColumn<PostBuildAction, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<PostBuildAction, Void>(Model.of(_T("Description"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<PostBuildAction>> cellItem, String componentId, IModel<PostBuildAction> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getDescription()));
			}

		});

		columns.add(new AbstractColumn<PostBuildAction, Void>(Model.of(_T("Condition"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<PostBuildAction>> cellItem, String componentId, IModel<PostBuildAction> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getCondition()));
			}

		});

		return columns;
	}

}
