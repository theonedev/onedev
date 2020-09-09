package io.onedev.server.web.editable.job.paramspec;

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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;

import io.onedev.server.buildspec.job.paramspec.ParamSpec;
import io.onedev.server.web.asset.inputspec.InputSpecCssResourceReference;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.editable.PropertyEditor;
import io.onedev.server.web.editable.PropertyUpdating;

@SuppressWarnings("serial")
class ParamSpecListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<ParamSpec> params;
	
	public ParamSpecListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		params = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			params.add((ParamSpec) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new ParamSpecEditPanel(id, params, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(ParamSpecListEditPanel.this);
					}

				};
			}
			
		});
		
		List<IColumn<ParamSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
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
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
		});		
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				cellItem.add(new Label(componentId, EditableUtils.getDisplayName(rowModel.getObject().getClass())));
			}
		});		
		
		columns.add(new AbstractColumn<ParamSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ParamSpec>> cellItem, String componentId, IModel<ParamSpec> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", ParamSpecListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new ParamSpecEditPanel(id, params, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(ParamSpecListEditPanel.this);
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						params.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(ParamSpecListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-right";
			}
			
		});		
		
		IDataProvider<ParamSpec> dataProvider = new ListDataProvider<ParamSpec>() {

			@Override
			protected List<ParamSpec> getData() {
				return params;			
			}

		};
		
		DataTable<ParamSpec, Void> dataTable;
		add(dataTable = new DataTable<ParamSpec, Void>("paramSpecs", columns, dataProvider, Integer.MAX_VALUE));
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
						Collections.swap(params, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(params, fromIndex-i, fromIndex-i-1);
				}
				onPropertyUpdating(target);
				target.add(ParamSpecListEditPanel.this);
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
		for (ParamSpec each: params)
			value.add(each);
		return value;
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new InputSpecCssResourceReference()));
		response.render(CssHeaderItem.forReference(new ParamSpecCssResourceReference()));
	}
	
}
