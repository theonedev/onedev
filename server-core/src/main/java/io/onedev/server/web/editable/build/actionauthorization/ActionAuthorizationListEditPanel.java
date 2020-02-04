package io.onedev.server.web.editable.build.actionauthorization;

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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
class ActionAuthorizationListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<ActionAuthorization> authorizations;
	
	public ActionAuthorizationListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		authorizations = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			authorizations.add((ActionAuthorization) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new ActionAuthorizationEditPanel(id, authorizations, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(ActionAuthorizationListEditPanel.this);
					}

				};
			}
			
		});
		
		List<IColumn<ActionAuthorization, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, IModel<ActionAuthorization> rowModel) {
				cellItem.add(new Label(componentId, "<span class=\"drag-indicator fa fa-reorder\"></span>").setEscapeModelStrings(false));
			}
			
			@Override
			public String getCssClass() {
				return "minimum actions";
			}
			
		});		
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("Action")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, IModel<ActionAuthorization> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getActionDescription()));
			}
		});		
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("Authorized Branches")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, IModel<ActionAuthorization> rowModel) {
				String authorizedBranches = rowModel.getObject().getAuthorizedBranches();
				if (authorizedBranches != null)
					cellItem.add(new Label(componentId, authorizedBranches));
				else
					cellItem.add(new Label(componentId, "<i>All</i>").setEscapeModelStrings(false));
			}
			
		});		
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, IModel<ActionAuthorization> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", ActionAuthorizationListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new ActionAuthorizationEditPanel(id, authorizations, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(ActionAuthorizationListEditPanel.this);
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						authorizations.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(ActionAuthorizationListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});		
		
		IDataProvider<ActionAuthorization> dataProvider = new ListDataProvider<ActionAuthorization>() {

			@Override
			protected List<ActionAuthorization> getData() {
				return authorizations;			
			}

		};
		
		DataTable<ActionAuthorization, Void> dataTable;
		add(dataTable = new DataTable<ActionAuthorization, Void>("authorizations", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(authorizations, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(authorizations, fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
				target.add(ActionAuthorizationListEditPanel.this);
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
		for (ActionAuthorization each: authorizations)
			value.add(each);
		return value;
	}

}
