package io.onedev.server.web.editable.buildspec.job.jobdependency;

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

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;
import io.onedev.server.web.util.TextUtils;

class JobDependencyListEditPanel extends DrawCardBeanListEditPanel<JobDependency> {

	private static final long serialVersionUID = 1L;

	JobDependencyListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	private BuildSpec getBuildSpec() {
		BuildSpecAware buildSpecAware = findParent(BuildSpecAware.class);
		return buildSpecAware != null ? buildSpecAware.getBuildSpec() : null;
	}

	private Job getJob() {
		JobAware jobAware = findParent(JobAware.class);
		return jobAware != null ? jobAware.getJob() : null;
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new job dependency");
	}

	@Override
	protected DrawCardBeanItemEditPanel<JobDependency> newEditPanel(String id, List<JobDependency> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new JobDependencyEditPanel(id, items, index, callback) {

			private static final long serialVersionUID = 1L;

			@Override
			public BuildSpec getBuildSpec() {
				return JobDependencyListEditPanel.this.getBuildSpec();
			}

			@Override
			public Job getJob() {
				return JobDependencyListEditPanel.this.getJob();
			}

			@Override
			public List<ParamSpec> getParamSpecs() {
				return getJob() != null ? getJob().getParamSpecs() : null;
			}

		};
	}

	@Override
	protected List<IColumn<JobDependency, Void>> getDataColumns() {
		List<IColumn<JobDependency, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<JobDependency, Void>(Model.of(_T("Job"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getJobName()));
			}

		});

		columns.add(new AbstractColumn<JobDependency, Void>(Model.of(_T("#Params"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getParamMatrix().size()));
			}

		});

		columns.add(new AbstractColumn<JobDependency, Void>(Model.of(_T("Require Successful"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new Label(componentId, TextUtils.getDisplayValue(rowModel.getObject().isRequireSuccessful())));
			}

		});

		return columns;
	}

}
