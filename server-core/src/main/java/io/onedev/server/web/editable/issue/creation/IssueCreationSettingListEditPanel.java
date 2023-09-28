package io.onedev.server.web.editable.issue.creation;

import io.onedev.server.model.support.administration.IssueCreationSetting;
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
class IssueCreationSettingListEditPanel extends PropertyEditor<List<Serializable>> {

	private final List<IssueCreationSetting> settings;
	
	public IssueCreationSettingListEditPanel(String id, PropertyDescriptor propertyDescriptor, IModel<List<Serializable>> model) {
		super(id, propertyDescriptor, model);
		
		settings = new ArrayList<>();
		for (Serializable each: model.getObject()) {
			settings.add((IssueCreationSetting) each);
		}
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new IssueCreationSettingEditPanel(id, settings, -1) {

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						markFormDirty(target);
						modal.close();
						onPropertyUpdating(target);
						target.add(IssueCreationSettingListEditPanel.this);
					}

				};
			}
			
		});
		
		List<IColumn<IssueCreationSetting, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
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
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("Sender Emails")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
				IssueCreationSetting setting = rowModel.getObject();
				if (setting.getSenderEmails() != null)
					cellItem.add(new Label(componentId, setting.getSenderEmails()));
				else
					cellItem.add(new Label(componentId, "<i>Any sender</i>").setEscapeModelStrings(false));
			}
		});		
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("Applicable Projects")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
				IssueCreationSetting setting = rowModel.getObject();
				if (setting.getApplicableProjects() != null)
					cellItem.add(new Label(componentId, setting.getApplicableProjects()));
				else
					cellItem.add(new Label(componentId, "<i>Any project</i>").setEscapeModelStrings(false));
			}
		});		
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", IssueCreationSettingListEditPanel.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new IssueCreationSettingEditPanel(id, settings, cellItem.findParent(Item.class).getIndex()) {

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected void onSave(AjaxRequestTarget target) {
								markFormDirty(target);
								modal.close();
								onPropertyUpdating(target);
								target.add(IssueCreationSettingListEditPanel.this);
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						markFormDirty(target);
						settings.remove(rowModel.getObject());
						onPropertyUpdating(target);
						target.add(IssueCreationSettingListEditPanel.this);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions minimum";
			}
			
		});		
		
		IDataProvider<IssueCreationSetting> dataProvider = new ListDataProvider<IssueCreationSetting>() {

			@Override
			protected List<IssueCreationSetting> getData() {
				return settings;			
			}

		};
		
		DataTable<IssueCreationSetting, Void> dataTable;
		add(dataTable = new DataTable<IssueCreationSetting, Void>("settings", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable, Model.of("Not defined")));
		dataTable.add(new NoRecordsBehavior());
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				CollectionUtils.move(settings, from.getItemIndex(), to.getItemIndex());
				onPropertyUpdating(target);
				target.add(IssueCreationSettingListEditPanel.this);
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
		for (IssueCreationSetting each: settings)
			value.add(each);
		return value;
	}

	@Override
	public boolean needExplicitSubmit() {
		return true;
	}

}
