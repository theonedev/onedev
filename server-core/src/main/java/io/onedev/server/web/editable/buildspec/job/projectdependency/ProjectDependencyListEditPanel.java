package io.onedev.server.web.editable.buildspec.job.projectdependency;

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
import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanItemEditPanel;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListEditPanel;

class ProjectDependencyListEditPanel extends DrawCardBeanListEditPanel<ProjectDependency> {

	private static final long serialVersionUID = 1L;

	ProjectDependencyListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
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
		return _T("Add new project dependency");
	}

	@Override
	protected DrawCardBeanItemEditPanel<ProjectDependency> newEditPanel(String id, List<ProjectDependency> items,
			int index, DrawCardBeanItemEditPanel.EditCallback callback) {
		return new ProjectDependencyEditPanel(id, items, index, callback) {

			private static final long serialVersionUID = 1L;

			@Override
			public BuildSpec getBuildSpec() {
				return ProjectDependencyListEditPanel.this.getBuildSpec();
			}

			@Override
			public Job getJob() {
				return ProjectDependencyListEditPanel.this.getJob();
			}

			@Override
			public List<ParamSpec> getParamSpecs() {
				return getJob() != null ? getJob().getParamSpecs() : null;
			}

		};
	}

	@Override
	protected List<IColumn<ProjectDependency, Void>> getDataColumns() {
		List<IColumn<ProjectDependency, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of(_T("Project"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getProjectPath()));
			}

		});

		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of(_T("Build"))) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getBuildProvider().getDescription()));
			}

		});

		return columns;
	}

}
