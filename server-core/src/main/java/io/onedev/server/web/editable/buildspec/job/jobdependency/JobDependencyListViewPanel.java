package io.onedev.server.web.editable.buildspec.job.jobdependency;

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
				cellItem.add(new Label(componentId, rowModel.getObject().getJobName()));
			}
			
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("#Params")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getJobParams().size()));
			}
			
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("Require Successful")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				JobDependency dependency = rowModel.getObject();
				cellItem.add(new Label(componentId, TextUtils.getDisplayValue(dependency.isRequireSuccessful())));
			}
			
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				int dependencyIndex = cellItem.findParent(Item.class).getIndex();
				Fragment fragment = new Fragment(componentId, "showDetailFrag", JobDependencyListViewPanel.this);
				fragment.add(new AjaxLink<Void>("link") {

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
	
	
}
