package io.onedev.server.web.page.admin.servicedesk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.DefaultProjectDesignation;
import io.onedev.server.model.support.administration.ServiceDeskSetting;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class DefaultProjectDesignationListPage extends AdministrationPage {

	private final List<DefaultProjectDesignation> designations;
	
	public DefaultProjectDesignationListPage(PageParameters params) {
		super(params);
		designations = getSettingManager().getServiceDeskSetting().getDefaultProjectDesignations();
	}

	private DataTable<DefaultProjectDesignation, Void> designationsTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new DefaultProjectDesignationEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						saveSettings();
						target.add(designationsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected List<DefaultProjectDesignation> getSettings() {
						return designations;
					}

				};
			}

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}
			
		});
		
		List<IColumn<DefaultProjectDesignation, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<DefaultProjectDesignation, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<DefaultProjectDesignation>> cellItem, String componentId, IModel<DefaultProjectDesignation> rowModel) {
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
		
		columns.add(new AbstractColumn<DefaultProjectDesignation, Void>(Model.of("Applicable Senders")) {

			@Override
			public void populateItem(Item<ICellPopulator<DefaultProjectDesignation>> cellItem, String componentId, IModel<DefaultProjectDesignation> rowModel) {
				DefaultProjectDesignation designation = rowModel.getObject();
				if (designation.getSenderEmails() != null)
					cellItem.add(new Label(componentId, designation.getSenderEmails()));
				else
					cellItem.add(new Label(componentId, "<i>Any sender</i>").setEscapeModelStrings(false));
			}
			
		});		
		
		columns.add(new AbstractColumn<DefaultProjectDesignation, Void>(Model.of("Default Project")) {

			@Override
			public void populateItem(Item<ICellPopulator<DefaultProjectDesignation>> cellItem, String componentId, IModel<DefaultProjectDesignation> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getDefaultProject()));
			}
			
		});		
		
		columns.add(new AbstractColumn<DefaultProjectDesignation, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<DefaultProjectDesignation>> cellItem, String componentId, IModel<DefaultProjectDesignation> rowModel) {
				int designationIndex = cellItem.findParent(LoopItem.class).getIndex();
				
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", DefaultProjectDesignationListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new DefaultProjectDesignationEditPanel(id, designationIndex) {

							@Override
							protected void onSave(AjaxRequestTarget target) {
								saveSettings();
								target.add(designationsTable);
								modal.close();
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected List<DefaultProjectDesignation> getSettings() {
								return designations;
							}

						};
					}
					
					@Override
					protected String getModalCssClass() {
						return "modal-lg";
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this entry?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						designations.remove(designationIndex);
						saveSettings();
						target.add(designationsTable);
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}

		});		
		
		IDataProvider<DefaultProjectDesignation> dataProvider = new ListDataProvider<DefaultProjectDesignation>() {

			@Override
			protected List<DefaultProjectDesignation> getData() {
				return designations;
			}

		};
		
		add(designationsTable = new DataTable<DefaultProjectDesignation, Void>("designations", columns, dataProvider, Integer.MAX_VALUE));
		designationsTable.addTopToolbar(new HeadersToolbar<Void>(designationsTable, null));
		designationsTable.addBottomToolbar(new NoRecordsToolbar(designationsTable));
		designationsTable.add(new NoRecordsBehavior());
		designationsTable.setOutputMarkupId(true);
		
		designationsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(designations, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(designations, fromIndex-i, fromIndex-i-1);
				}
				saveSettings();
				target.add(designationsTable);
			}
			
		}.sortable("tbody"));
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Default Projects");
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	private void saveSettings() {
		ServiceDeskSetting designation = getSettingManager().getServiceDeskSetting();
		designation.setDefaultProjectDesignations(designations);
		getSettingManager().saveServiceDeskSetting(designation);
	}
	
}
