package io.onedev.server.web.editable.workspacespec.cacheconfig;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.ArrayList;
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

import io.onedev.server.model.support.workspace.spec.CacheConfig;
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
import io.onedev.server.web.util.TextUtils;

class CacheConfigListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<CacheConfig> configs;
	
	public CacheConfigListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		configs = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			configs.add((CacheConfig) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new CacheConfigEditPanel(id, configs, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(CacheConfigListEditPanel.this);
					}
					
				};
			}
			
		});
		
		List<IColumn<CacheConfig, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
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
		
		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of(_T("Cache Key"))) {

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getKey()));
			}

		});		

		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of(_T("#Paths"))) {

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getPaths().size()));
			}
			
		});		

		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of(_T("Upload Strategy"))) {

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				cellItem.add(new Label(componentId, TextUtils.getDisplayValue(rowModel.getObject().getUploadStrategy())));
			}
			
		});		
		
		columns.add(new AbstractColumn<CacheConfig, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<CacheConfig>> cellItem, String componentId, IModel<CacheConfig> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", CacheConfigListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new CacheConfigEditPanel(id, configs, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(CacheConfigListEditPanel.this);
							}
							
						};
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						configs.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(CacheConfigListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "minimum actions";
			}
			
		});		
		
		IDataProvider<CacheConfig> dataProvider = new ListDataProvider<CacheConfig>() {

			@Override
			protected List<CacheConfig> getData() {
				return configs;			
			}

		};
		
		DataTable<CacheConfig, Void> dataTable;
		add(dataTable = new DataTable<CacheConfig, Void>("configs", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of(_T("Unspecified"))));
		dataTable.add(new NoRecordsBehavior());
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				CollectionUtils.move(configs, from.getItemIndex(), to.getItemIndex());
				onPropertyUpdating(target);
				target.add(CacheConfigListEditPanel.this);
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
		for (CacheConfig each: configs)
			value.add(each);
		return value;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
