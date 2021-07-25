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
import io.onedev.server.model.support.administration.IssueCreationSetting;
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
public class IssueCreationSettingListPage extends AdministrationPage {

	private final List<IssueCreationSetting> issueCreationSettings;
	
	public IssueCreationSettingListPage(PageParameters params) {
		super(params);
		issueCreationSettings = getSettingManager().getServiceDeskSetting().getIssueCreationSettings();
	}

	private DataTable<IssueCreationSetting, Void> issueCreationSettingsTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new IssueCreationSettingEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						saveIssueCreationSettings();
						target.add(issueCreationSettingsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected List<IssueCreationSetting> getIssueCreationSettings() {
						return issueCreationSettings;
					}

				};
			}

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
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
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("Applicable Senders")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
				IssueCreationSetting issueCreationSetting = rowModel.getObject();
				if (issueCreationSetting.getSenderEmails() != null)
					cellItem.add(new Label(componentId, issueCreationSetting.getSenderEmails()));
				else
					cellItem.add(new Label(componentId, "<i>Any sender</i>").setEscapeModelStrings(false));
			}
			
		});		
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("Applicable Projects")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
				IssueCreationSetting issueCreationSetting = rowModel.getObject();
				if (issueCreationSetting.getApplicableProjects() != null)
					cellItem.add(new Label(componentId, issueCreationSetting.getApplicableProjects()));
				else
					cellItem.add(new Label(componentId, "<i>Any project</i>").setEscapeModelStrings(false));
			}
			
		});		
		
		columns.add(new AbstractColumn<IssueCreationSetting, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<IssueCreationSetting>> cellItem, String componentId, IModel<IssueCreationSetting> rowModel) {
				int issueCreationSettingIndex = cellItem.findParent(LoopItem.class).getIndex();
				
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", IssueCreationSettingListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new IssueCreationSettingEditPanel(id, issueCreationSettingIndex) {

							@Override
							protected void onSave(AjaxRequestTarget target) {
								saveIssueCreationSettings();
								target.add(issueCreationSettingsTable);
								modal.close();
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected List<IssueCreationSetting> getIssueCreationSettings() {
								return issueCreationSettings;
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
						issueCreationSettings.remove(issueCreationSettingIndex);
						saveIssueCreationSettings();
						target.add(issueCreationSettingsTable);
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}

		});		
		
		IDataProvider<IssueCreationSetting> dataProvider = new ListDataProvider<IssueCreationSetting>() {

			@Override
			protected List<IssueCreationSetting> getData() {
				return issueCreationSettings;
			}

		};
		
		add(issueCreationSettingsTable = new DataTable<IssueCreationSetting, Void>("issueCreationSettings", columns, dataProvider, Integer.MAX_VALUE));
		issueCreationSettingsTable.addTopToolbar(new HeadersToolbar<Void>(issueCreationSettingsTable, null));
		issueCreationSettingsTable.addBottomToolbar(new NoRecordsToolbar(issueCreationSettingsTable));
		issueCreationSettingsTable.add(new NoRecordsBehavior());
		issueCreationSettingsTable.setOutputMarkupId(true);
		
		issueCreationSettingsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(issueCreationSettings, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(issueCreationSettings, fromIndex-i, fromIndex-i-1);
				}
				saveIssueCreationSettings();
				target.add(issueCreationSettingsTable);
			}
			
		}.sortable("tbody"));
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Issue Creation Settings");
	}
	
	private SettingManager getSettingManager() {
		return OneDev.getInstance(SettingManager.class);
	}
	
	private void saveIssueCreationSettings() {
		ServiceDeskSetting setting = getSettingManager().getServiceDeskSetting();
		setting.setIssueCreationSettings(issueCreationSettings);
		getSettingManager().saveServiceDeskSetting(setting);
	}
	
}
