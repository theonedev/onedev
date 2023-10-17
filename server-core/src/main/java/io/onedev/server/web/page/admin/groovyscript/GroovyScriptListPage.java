package io.onedev.server.web.page.admin.groovyscript;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GroovyScript;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.AdministrationPage;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
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

import java.util.ArrayList;
import java.util.List;

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
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
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
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
		});		
		
		columns.add(new AbstractColumn<GroovyScript, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				int scriptIndex = cellItem.findParent(LoopItem.class).getIndex();
				
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", GroovyScriptListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
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
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this script?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						scripts.remove(scriptIndex);
						OneDev.getInstance(SettingManager.class).saveGroovyScripts(scripts);
						target.add(scriptsTable);
					}
					
				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
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
				CollectionUtils.move(scripts, from.getItemIndex(), to.getItemIndex());
				OneDev.getInstance(SettingManager.class).saveGroovyScripts(scripts);
				target.add(scriptsTable);
			}
			
		}.sortable("tbody"));
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Groovy Scripts");
	}
	
}
