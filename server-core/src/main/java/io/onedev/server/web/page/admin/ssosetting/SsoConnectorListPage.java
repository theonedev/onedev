package io.onedev.server.web.page.admin.ssosetting;

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
import io.onedev.server.manager.SettingManager;
import io.onedev.server.manager.UserManager;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.link.copytoclipboard.CopyToClipboardLink;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.AdministrationPage;

@SuppressWarnings("serial")
public class SsoConnectorListPage extends AdministrationPage {

	private final List<SsoConnector> connectors;
	
	public SsoConnectorListPage(PageParameters params) {
		super(params);
		connectors = OneDev.getInstance(SettingManager.class).getSsoConnectors();
	}

	private DataTable<SsoConnector, Void> connectorsTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new SsoConnectorEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(connectorsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected List<SsoConnector> getConnectors() {
						return connectors;
					}

				};
			}
			
		});
		
		List<IColumn<SsoConnector, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
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
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
		});		
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("Callback URL")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
				SsoConnector connector = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "callbackUriFrag", SsoConnectorListPage.this);
				fragment.add(new Label("value", connector.getCallbackUri().toString()));
				fragment.add(new CopyToClipboardLink("copy", Model.of(connector.getCallbackUri().toString())));
				cellItem.add(fragment);
			}
			
		});		
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
				int connectorIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", SsoConnectorListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new SsoConnectorEditPanel(id, connectorIndex) {

							@Override
							protected void onSave(AjaxRequestTarget target) {
								target.add(connectorsTable);
								modal.close();
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected List<SsoConnector> getConnectors() {
								return connectors;
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this connector?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						SsoConnector connector = connectors.remove(connectorIndex);
						OneDev.getInstance(TransactionManager.class).run(new Runnable() {

							@Override
							public void run() {
								OneDev.getInstance(SettingManager.class).saveSsoConnectors(connectors);
								OneDev.getInstance(UserManager.class).onDeleteSsoConnector(connector.getName());
							}
							
						});
						target.add(connectorsTable);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});		
		
		IDataProvider<SsoConnector> dataProvider = new ListDataProvider<SsoConnector>() {

			@Override
			protected List<SsoConnector> getData() {
				return connectors;
			}

		};
		
		add(connectorsTable = new DataTable<SsoConnector, Void>("connectors", columns, dataProvider, Integer.MAX_VALUE));
		connectorsTable.addTopToolbar(new HeadersToolbar<Void>(connectorsTable, null));
		connectorsTable.addBottomToolbar(new NoRecordsToolbar(connectorsTable));
		connectorsTable.add(new NoRecordsBehavior());
		connectorsTable.setOutputMarkupId(true);
		
		connectorsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(connectors, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(connectors, fromIndex-i, fromIndex-i-1);
				}
				
				OneDev.getInstance(SettingManager.class).saveSsoConnectors(connectors);
				target.add(connectorsTable);
			}
			
		}.sortable("tbody"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Single Sign On</span>").setEscapeModelStrings(false);
	}
	
}
