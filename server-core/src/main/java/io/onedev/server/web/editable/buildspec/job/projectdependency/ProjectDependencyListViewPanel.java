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

import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.web.editable.drawcardbeanlist.DrawCardBeanListViewPanel;

class ProjectDependencyListViewPanel extends DrawCardBeanListViewPanel<ProjectDependency> {

	private static final long serialVersionUID = 1L;

	ProjectDependencyListViewPanel(String id, List<Serializable> elements) {
		super(id, elements);
	}

	@Override
	protected String getDetailTitle(ProjectDependency item) {
		return _T("Project Dependency");
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
