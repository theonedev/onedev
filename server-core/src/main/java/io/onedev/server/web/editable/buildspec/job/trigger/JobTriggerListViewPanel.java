package io.onedev.server.web.editable.buildspec.job.trigger;

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

import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class JobTriggerListViewPanel extends DrawCardBeanListViewPanel<JobTrigger> {

	private static final long serialVersionUID = 1L;

	JobTriggerListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(JobTrigger item) {
		String triggerType = EditableUtils.getDisplayName(item.getClass());
		return _T("Trigger") + " (" + _T("type") + ": " + triggerType + ")";
	}

	@Override
	protected List<IColumn<JobTrigger, Void>> getDataColumns() {
		List<IColumn<JobTrigger, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<JobTrigger, Void>(Model.of(_T("Description"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getDescription()));
			}

		});

		columns.add(new AbstractColumn<JobTrigger, Void>(Model.of(_T("#Params"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getParamMatrix().size()));
			}

		});

		return columns;
	}

}
