package io.onedev.server.web.page.admin.groovyscript;

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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
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
import io.onedev.server.model.support.administration.GroovyScript;
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
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.util.TextUtils;

@SuppressWarnings("serial")
public class GroovyScriptListPage extends AdministrationPage {

	private final List<GroovyScript> scripts;
	
	public GroovyScriptListPage(PageParameters params) {
		super(params);
		scripts = OneDev.getInstance(SettingManager.class).getGroovyScripts();
	}

	private DataTable<GroovyScript, Void> scriptsTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new GroovyScriptEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(scriptsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected List<GroovyScript> getScripts() {
						return scripts;
					}

				};
			}

			@Override
			protected String getModalCssClass() {
				return "modal-lg";
			}
			
		});
		
		List<IColumn<GroovyScript, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				String html = String.format("<svg class='drag-indicator icon'><use xlink:href='%s'/></svg> %s", 
						SpriteImage.getVersionedHref("grip"), HtmlEscape.escapeHtml5(rowModel.getObject().getName()));
				cellItem.add(new Label(componentId, html).setEscapeModelStrings(false));
			}
		});		
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("Can be Used by Build Jobs")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				cellItem.add(new Label(componentId, TextUtils.describe(rowModel.getObject().isCanBeUsedByBuildJobs())));
			}
			
		});		
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				int scriptIndex = cellItem.findParent(LoopItem.class).getIndex();
				
				Fragment fragment = new Fragment(componentId, "showDetailFrag", GroovyScriptListPage.this);
				fragment.add(new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new OffCanvasCardPanel(target, OffCanvasPanel.Placement.RIGHT, null) {

							@Override
							protected Component newTitle(String componentId) {
								return new Label(componentId, scripts.get(scriptIndex).getName());
							}

							@Override
							protected void onInitialize() {
								super.onInitialize();
								add(AttributeAppender.append("class", "groovy-script"));
							}

							@Override
							protected Component newBody(String componentId) {
								return BeanContext.view(componentId, scripts.get(scriptIndex), Sets.newHashSet("name"), true);
							}
							
							@Override
							protected Component newFooter(String componentId) {
								Fragment fragment = new Fragment(componentId, "scriptActionsFrag", GroovyScriptListPage.this);
								fragment.add(new ModalLink("edit") {

									@Override
									protected Component newContent(String id, ModalPanel modal) {
										close();
										return new GroovyScriptEditPanel(id, scriptIndex) {

											@Override
											protected void onSave(AjaxRequestTarget target) {
												target.add(scriptsTable);
												modal.close();
											}

											@Override
											protected void onCancel(AjaxRequestTarget target) {
												modal.close();
											}

											@Override
											protected List<GroovyScript> getScripts() {
												return scripts;
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
										attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this script?"));
									}

									@Override
									public void onClick(AjaxRequestTarget target) {
										scripts.remove(scriptIndex);
										OneDev.getInstance(SettingManager.class).saveGroovyScripts(scripts);
										target.add(scriptsTable);
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
		
		IDataProvider<GroovyScript> dataProvider = new ListDataProvider<GroovyScript>() {

			@Override
			protected List<GroovyScript> getData() {
				return scripts;
			}

		};
		
		add(scriptsTable = new DataTable<GroovyScript, Void>("scripts", columns, dataProvider, Integer.MAX_VALUE));
		scriptsTable.addTopToolbar(new HeadersToolbar<Void>(scriptsTable, null));
		scriptsTable.addBottomToolbar(new NoRecordsToolbar(scriptsTable));
		scriptsTable.add(new NoRecordsBehavior());
		scriptsTable.setOutputMarkupId(true);
		
		scriptsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(scripts, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(scripts, fromIndex-i, fromIndex-i-1);
				}
				
				OneDev.getInstance(SettingManager.class).saveGroovyScripts(scripts);
				target.add(scriptsTable);
			}
			
		}.sortable("tbody"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new GroovyScriptResourceReference()));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Groovy Scripts");
	}
	
}
