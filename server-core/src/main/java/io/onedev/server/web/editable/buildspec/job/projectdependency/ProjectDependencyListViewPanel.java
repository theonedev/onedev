package io.onedev.server.web.editable.buildspec.job.projectdependency;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.buildspec.job.projectdependency.ProjectDependency;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.editable.BeanContext;

class ProjectDependencyListViewPanel extends Panel {

	private final List<ProjectDependency> dependencies = new ArrayList<>();
	
	public ProjectDependencyListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			dependencies.add((ProjectDependency) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<ProjectDependency, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of("Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getProjectPath()));
			}
			
		});		
		
		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of("Build")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				ProjectDependency dependency = rowModel.getObject();
				cellItem.add(new Label(componentId, dependency.getBuildProvider().getDescription()));
			}
			
		});		
		
		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				int dependencyIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", ProjectDependencyListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								return new Label(componentId, "Project Dependency");
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "project-dependency"));
							}

							@Override
							protected Component newBody(String id) {
								return BeanContext.view(id, dependencies.get(dependencyIndex));
							}

						};
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "ellipsis text-right";
			}
			
		});	
		
		IDataProvider<ProjectDependency> dataProvider = new ListDataProvider<ProjectDependency>() {

			@Override
			protected List<ProjectDependency> getData() {
				return dependencies;
			}

		};
		
		add(new DataTable<ProjectDependency, Void>("dependencies", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of(_T("Unspecified"))));
				add(new NoRecordsBehavior());
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectDependencyCssResourceReference()));
	}
	
}
