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

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;

class JobTriggerListEditPanel extends DrawCardBeanListEditPanel<JobTrigger> {

	private static final long serialVersionUID = 1L;

	JobTriggerListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	private Job getJob() {
		JobAware jobAware = findParent(JobAware.class);
		return jobAware != null ? jobAware.getJob() : null;
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new trigger");
	}

	@Override
	protected DrawCardBeanItemEditPanel<JobTrigger> newEditPanel(String id, List<JobTrigger> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new JobTriggerEditPanel(id, items, index, callback) {

			private static final long serialVersionUID = 1L;

			@Override
			public Job getJob() {
				return JobTriggerListEditPanel.this.getJob();
			}

			@Override
			public List<ParamSpec> getParamSpecs() {
				return getJob() != null ? getJob().getParamSpecs() : null;
			}

		};
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
