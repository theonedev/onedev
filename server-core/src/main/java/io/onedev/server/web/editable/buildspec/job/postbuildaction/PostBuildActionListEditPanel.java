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

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.action.PostBuildAction;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;

class PostBuildActionListEditPanel extends DrawCardBeanListEditPanel<PostBuildAction> {

	private static final long serialVersionUID = 1L;

	PostBuildActionListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
	}

	private Job getJob() {
		JobAware jobAware = findParent(JobAware.class);
		return jobAware != null ? jobAware.getJob() : null;
	}

	private BuildSpec getBuildSpec() {
		BuildSpecAware buildSpecAware = findParent(BuildSpecAware.class);
		return buildSpecAware != null ? buildSpecAware.getBuildSpec() : null;
	}

	@Override
	protected String getAddTooltip() {
		return _T("Add new post-build action");
	}

	@Override
	protected DrawCardBeanItemEditPanel<PostBuildAction> newEditPanel(String id, List<PostBuildAction> items, int index,
			DrawCardBeanItemEditPanel.EditCallback callback) {
		return new PostBuildActionEditPanel(id, items, index, callback) {

			private static final long serialVersionUID = 1L;

			@Override
			public Job getJob() {
				return PostBuildActionListEditPanel.this.getJob();
			}

			@Override
			public BuildSpec getBuildSpec() {
				return PostBuildActionListEditPanel.this.getBuildSpec();
			}

			@Override
			public List<ParamSpec> getParamSpecs() {
				return getJob() != null ? getJob().getParamSpecs() : null;
			}

		};
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
