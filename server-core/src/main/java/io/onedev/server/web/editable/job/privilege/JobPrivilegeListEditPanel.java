package io.onedev.server.web.editable.job.privilege;

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

import io.onedev.server.model.support.role.JobPrivilege;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
class JobPrivilegeListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<JobPrivilege> privileges;
	
	public JobPrivilegeListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		privileges = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			privileges.add((JobPrivilege) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new JobPrivilegeEditPanel(id, privileges, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(JobPrivilegeListEditPanel.this);
					}

				};
			}
			
		});
		
		List<IColumn<JobPrivilege, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobPrivilege, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobPrivilege>> cellItem, String componentId, IModel<JobPrivilege> rowModel) {
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
		
		columns.add(new AbstractColumn<JobPrivilege, Void>(Model.of("Job Names")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobPrivilege>> cellItem, String componentId, IModel<JobPrivilege> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getJobNames()));
			}
		});		
		
		columns.add(new AbstractColumn<JobPrivilege, Void>(Model.of("Privilege")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobPrivilege>> cellItem, String componentId, IModel<JobPrivilege> rowModel) {
				JobPrivilege privilege = rowModel.getObject();
				if (privilege.isManageJob())
					cellItem.add(new Label(componentId, "Manage Job"));
				else if (privilege.isRunJob())
					cellItem.add(new Label(componentId, "Run Job"));
				else if (privilege.isAccessLog())
					cellItem.add(new Label(componentId, "Access Log"));
				else if (privilege.getAccessibleReports() != null)
					cellItem.add(new Label(componentId, "Access Reports: " + privilege.getAccessibleReports()));
				else
					cellItem.add(new Label(componentId, "Access Artifacts"));
			}
		});		
		
		columns.add(new AbstractColumn<JobPrivilege, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobPrivilege>> cellItem, String componentId, IModel<JobPrivilege> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", JobPrivilegeListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new JobPrivilegeEditPanel(id, privileges, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(JobPrivilegeListEditPanel.this);
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						privileges.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(JobPrivilegeListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions minimum";
			}
			
		});		
		
		IDataProvider<JobPrivilege> dataProvider = new ListDataProvider<JobPrivilege>() {

			@Override
			protected List<JobPrivilege> getData() {
				return privileges;			
			}

		};
		
		DataTable<JobPrivilege, Void> dataTable;
		add(dataTable = new DataTable<JobPrivilege, Void>("privileges", columns, dataProvider, Integer.MAX_VALUE));
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
						Collections.swap(privileges, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(privileges, fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
				target.add(JobPrivilegeListEditPanel.this);
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
		for (JobPrivilege each: privileges)
			value.add(each);
		return value;
	}
	
}
