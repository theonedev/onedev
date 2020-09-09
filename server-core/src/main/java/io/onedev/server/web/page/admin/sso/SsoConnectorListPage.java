package io.onedev.server.web.page.admin.sso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.LoopItem;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.html.HtmlEscape;

import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.entitymanager.UserManager;
import io.onedev.server.model.support.administration.sso.SsoConnector;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasCardPanel;
import io.onedev.server.web.component.offcanvas.OffCanvasPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
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
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
				String html = String.format("<svg class='drag-indicator icon'><use xlink:href='%s'/></svg> %s", 
						SpriteImage.getVersionedHref("grip"), HtmlEscape.escapeHtml5(rowModel.getObject().getName()));
				cellItem.add(new Label(componentId, html).setEscapeModelStrings(false));
			}
		});		
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("Type")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
				cellItem.add(new Label(componentId, EditableUtils.getDisplayName(rowModel.getObject().getClass())));
			}
			
		});		
		
		columns.add(new AbstractColumn<SsoConnector, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<SsoConnector>> cellItem, String componentId, IModel<SsoConnector> rowModel) {
				Fragment fragment = new Fragment(componentId, "showDetailFrag", SsoConnectorListPage.this);
				int connectorIndex = cellItem.findParent(LoopItem.class).getIndex();
				
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								return new Label(componentId, rowModel.getObject().getName());
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "sso-connector"));
							}

							@Override
							protected Component newBody(String id) {
								Fragment fragment = new Fragment(id, "connectorDetailFrag", SsoConnectorListPage.this);
								
								fragment.add(connectors.get(connectorIndex).renderAdditionalInfo("additionalInfo"));
								
								fragment.add(BeanContext.view("viewer", rowModel.getObject(), Sets.newHashSet("name"), true));
								fragment.setOutputMarkupId(true);
								
								return fragment;
							}

							@Override
							protected Component newFooter(String componentId) {
								Fragment fragment = new Fragment(componentId, "connectorActionsFrag", SsoConnectorListPage.this);
								fragment.add(new ModalLink("edit") {

									@Override
									protected Component newContent(String id, ModalPanel modal) {
										close();
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
										close();
									}
									
								});
								return fragment;
							}

						};		
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "text-right";
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
		return new Label(componentId, "<span class='text-truncate'>Single Sign On Providers</span>").setEscapeModelStrings(false);
	}
	
}
