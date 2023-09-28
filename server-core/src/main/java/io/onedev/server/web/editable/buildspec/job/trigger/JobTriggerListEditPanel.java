package io.onedev.server.web.editable.buildspec.job.trigger;

import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.JobAware;
import io.onedev.server.buildspec.job.trigger.JobTrigger;
import io.onedev.server.buildspec.param.spec.ParamSpec;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
class JobTriggerListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<JobTrigger> triggers;
	
	public JobTriggerListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		triggers = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			triggers.add((JobTrigger) each);
		}
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
				return new JobTriggerEditPanel(id, triggers, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(JobTriggerListEditPanel.this);
					}

					@Override
					public Job getJob() {
						return JobTriggerListEditPanel.this.getJob();
					}

					@Override
					public List<ParamSpec> getParamSpecs() {
						return getJob()!=null? getJob().getParamSpecs(): null;
					}
					
				};
			}
			
		});
		
		List<IColumn<JobTrigger, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobTrigger, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
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
		
		columns.add(new AbstractColumn<JobTrigger, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getDescription()));
			}
		});		
		
		columns.add(new AbstractColumn<JobTrigger, Void>(Model.of("#Params")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getParams().size()));
			}
			
		});		
		
		columns.add(new AbstractColumn<JobTrigger, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobTrigger>> cellItem, String componentId, IModel<JobTrigger> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", JobTriggerListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new JobTriggerEditPanel(id, triggers, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(JobTriggerListEditPanel.this);
							}

							@Override
							public Job getJob() {
								return JobTriggerListEditPanel.this.getJob();
							}

							@Override
							public List<ParamSpec> getParamSpecs() {
								return getJob()!=null? getJob().getParamSpecs(): null;
							}
							
						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						triggers.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(JobTriggerListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}
			
		});		
		
		IDataProvider<JobTrigger> dataProvider = new ListDataProvider<JobTrigger>() {

			@Override
			protected List<JobTrigger> getData() {
				return triggers;			
			}

		};
		
		DataTable<JobTrigger, Void> dataTable;
		add(dataTable = new DataTable<JobTrigger, Void>("triggers", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of("Not defined")));
		dataTable.add(new NoRecordsBehavior());
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				CollectionUtils.move(triggers, from.getItemIndex(), to.getItemIndex());
				onPropertyUpdating(target);
				target.add(JobTriggerListEditPanel.this);
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
		for (JobTrigger each: triggers)
			value.add(each);
		return value;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
