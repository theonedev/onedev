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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.web.ajaxlistener.ConfirmListener;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.admin.AdministrationPage;
import io.onedev.server.web.page.layout.SideFloating;
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
			
		});
		
		List<IColumn<GroovyScript, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(LoopItem.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, "<span class=\"drag-indicator fa fa-reorder\"></span> " 
								+ HtmlEscape.escapeHtml5(rowModel.getObject().getName())).setEscapeModelStrings(false);
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("Can be Used by Build Jobs")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				GroovyScript script = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, cellItem.findParent(LoopItem.class).getIndex()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, TextUtils.describe(script.isCanBeUsedByBuildJobs()));
					}
					
				});
			}
			
		});		
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				cellItem.add(new ColumnFragment(componentId, 0) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, "<i class='fa fa-ellipsis-h'></i>").setEscapeModelStrings(false);
					}
					
				});
				
			}

			@Override
			public String getCssClass() {
				return "ellipsis";
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
	
	private abstract class ColumnFragment extends Fragment {

		private final int scriptIndex;
		
		public ColumnFragment(String id, int scriptIndex) {
			super(id, "columnFrag", GroovyScriptListPage.this);
			this.scriptIndex = scriptIndex;
		}

		protected abstract Component newLabel(String componentId);
		
		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						private GroovyScript getScript() {
							return scripts.get(scriptIndex);
						}
						
						@Override
						protected String getTitle() {
							return getScript().getName();
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "groovy-script def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewScriptFrag", GroovyScriptListPage.this);
							fragment.add(BeanContext.view("viewer", getScript(), Sets.newHashSet("name"), true));
							fragment.add(new ModalLink("edit") {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									sideFloating.close();
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
								
							});
							fragment.add(new AjaxLink<Void>("delete") {

								@Override
								protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
									super.updateAjaxAttributes(attributes);
									attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this script?"));
								}

								@Override
								public void onClick(AjaxRequestTarget target) {
									scripts.remove(scriptIndex);
									OneDev.getInstance(SettingManager.class).saveGroovyScripts(scripts);
									target.add(scriptsTable);
									close();
								}
								
							});
							
							fragment.add(new NotificationPanel("feedback", fragment));
							fragment.setOutputMarkupId(true);
							
							return fragment;
						}

					};		
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
	}
}
