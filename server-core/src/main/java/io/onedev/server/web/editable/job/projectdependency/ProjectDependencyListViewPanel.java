package io.onedev.server.web.editable.job.projectdependency;

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
import org.apache.wicket.markup.ComponentTag;
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

import io.onedev.server.buildspec.job.ProjectDependency;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel.Placement;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;

@SuppressWarnings("serial")
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
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getProjectName());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of("Build")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getBuildNumber());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<ProjectDependency, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ProjectDependency>> cellItem, String componentId, IModel<ProjectDependency> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new SpriteImage(componentId, "ellipsis") {

							@Override
							protected void onComponentTag(ComponentTag tag) {
								super.onComponentTag(tag);
								tag.setName("svg");
								tag.put("class", "icon");
							}
							
						};
					}
					
				});
				
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
				addBottomToolbar(new NoRecordsToolbar(this, Model.of("Not defined")));
				add(new NoRecordsBehavior());
			}
			
		});
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, int index) {
			super(id, "columnFrag", ProjectDependencyListViewPanel.this);
			this.index = index;
		}
		
		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new OffCanvasCardPanel(target, Placement.RIGHT, null) {

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
							return BeanContext.view(id, dependencies.get(index));
						}
							
					};
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProjectDependencyCssResourceReference()));
	}
	
}
