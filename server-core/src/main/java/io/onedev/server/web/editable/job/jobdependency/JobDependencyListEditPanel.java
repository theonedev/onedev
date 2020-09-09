package io.onedev.server.web.editable.job.jobdependency;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.buildspec.BuildSpecAware;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.JobDependency;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import io.onedev.server.web.util.TextUtils;

@SuppressWarnings("serial")
class JobDependencyListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<JobDependency> dependencies;
	
	public JobDependencyListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		dependencies = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			dependencies.add((JobDependency) each);
		}
	}
	
	private BuildSpec getBuildSpec() {
		BuildSpecAware buildSpecAware = findParent(BuildSpecAware.class);
		if (buildSpecAware != null)
			return buildSpecAware.getBuildSpec();
		else
			return null;
	}

	private Job getJob() {
		JobAware jobAware = findParent(JobAware.class);
		if (jobAware != null)
			return jobAware.getJob();
		else
			return null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new JobDependencyEditPanel(id, dependencies, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(JobDependencyListEditPanel.this);
					}

					@Override
					public BuildSpec getBuildSpec() {
						return JobDependencyListEditPanel.this.getBuildSpec();
					}

					@Override
					public Job getJob() {
						return JobDependencyListEditPanel.this.getJob();
					}

				};
			}
			
		});
		
		List<IColumn<JobDependency, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				cellItem.add(new SpriteImage(componentId, "grip") {

					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						tag.setName("svg");
						tag.put("class", "icon drag-indicator");
					}
					
				});
			}
			
			@Override
			public String getCssClass() {
				return "minimum actions";
			}
			
		});		
		
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
				cellItem.add(new Label(componentId, TextUtils.describe(dependency.isRequireSuccessful())));
			}
		});		
		
		columns.add(new AbstractColumn<JobDependency, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobDependency>> cellItem, String componentId, IModel<JobDependency> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", JobDependencyListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new JobDependencyEditPanel(id, dependencies, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(JobDependencyListEditPanel.this);
							}

							@Override
							public BuildSpec getBuildSpec() {
								return JobDependencyListEditPanel.this.getBuildSpec();
							}

							@Override
							public Job getJob() {
								return JobDependencyListEditPanel.this.getJob();
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						dependencies.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(JobDependencyListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions minimum";
			}
			
		});		
		
		IDataProvider<JobDependency> dataProvider = new ListDataProvider<JobDependency>() {

			@Override
			protected List<JobDependency> getData() {
				return dependencies;			
			}

		};
		
		DataTable<JobDependency, Void> dataTable;
		add(dataTable = new DataTable<JobDependency, Void>("dependencies", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of("Not defined")));
		dataTable.add(new NoRecordsBehavior());
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(dependencies, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(dependencies, fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
				target.add(JobDependencyListEditPanel.this);
			}
			
		}.sortable("tbody"));
	}

	@Override
	public void onEvent(IEvent<?> event) {
		super.onEvent(event);
		
		if (event.getPayload() instanceof PropertyUpdating) {
			event.stop();
			onPropertyUpdating(((PropertyUpdating)event.getPayload()).getHandler());
		}		
	}

	@Override
	protected List<Serializable> convertInputToValue() throws ConversionException {
		List<Serializable> value = new ArrayList<>();
		for (JobDependency each: dependencies)
			value.add(each);
		return value;
	}

}
