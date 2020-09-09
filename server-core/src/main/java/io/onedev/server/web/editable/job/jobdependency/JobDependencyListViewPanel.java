package io.onedev.server.web.editable.job.jobdependency;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.TextUtils;

@SuppressWarnings("serial")
class JobDependencyListViewPanel extends Panel {

	private final List<JobDependency> dependencies = new ArrayList<>();
	
	public JobDependencyListViewPanel(String id, List<Serializable> elements) {
		super(id);
		
		for (Serializable each: elements)
			dependencies.add((JobDependency) each);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		List<IColumn<JobDependency, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("Job")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getJobName());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("#Params")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, rowModel.getObject().getJobParams().size());
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("Require Successful")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						JobDependency dependency = rowModel.getObject();
						return new Label(componentId, TextUtils.describe(dependency.isRequireSuccessful()));
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(Item.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						if (!rowModel.getObject().getJobParams().isEmpty()) {
							return new SpriteImage(componentId, "ellipsis") {

								@Override
								protected void onComponentTag(ComponentTag tag) {
									super.onComponentTag(tag);
									tag.setName("svg");
									tag.put("class", "icon");
								}
								
							};
						} else {
							return new Label(componentId);
						}
					}
					
				});
			}

			@Override
			public String getCssClass() {
				return "ellipsis text-right";
			}
			
		});		
		
		IDataProvider<JobDependency> dataProvider = new ListDataProvider<JobDependency>() {

			@Override
			protected List<JobDependency> getData() {
				return dependencies;
			}

		};
		
		add(new DataTable<JobDependency, Void>("dependencies", columns, dataProvider, Integer.MAX_VALUE) {

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new NoRecordsBehavior());
				addTopToolbar(new HeadersToolbar<Void>(this, null));
				addBottomToolbar(new NoRecordsToolbar(this, Model.of("Not defined")));
			}
			
		});
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, int index) {
			super(id, "columnFrag", JobDependencyListViewPanel.this);
			this.index = index;
		}
		
		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

						@Override
						protected Component newTitle(String componentId) {
							return new Label(componentId, "Job Dependency");
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "job-dependency"));
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
	
}
