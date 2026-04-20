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

import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;
import io.onedev.server.web.util.TextUtils;

class JobDependencyListViewPanel extends DrawCardBeanListViewPanel<JobDependency> {

	private static final long serialVersionUID = 1L;

	JobDependencyListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(JobDependency item) {
		return _T("Job Dependency");
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
