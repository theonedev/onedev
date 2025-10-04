package io.onedev.server.web.page.admin.groovyscript;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
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
import io.onedev.server.web.util.TextUtils;

public class GroovyScriptListPage extends AdministrationPage {

	private final List<GroovyScript> scripts;
	
	public GroovyScriptListPage(PageParameters params) {
		super(params);
		scripts = OneDev.getInstance(SettingService.class).getGroovyScripts();
	}

	private DataTable<GroovyScript, Void> scriptsTable;
	
	private WebMarkupContainer newEditLink(String componentId, int scriptIndex) {
		return new ModalLink(componentId) {

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

		};		
	}

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
		
		columns.add(new AbstractColumn<>(Model.of("")) {

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
		
		columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				var fragment = new Fragment(componentId, "nameColumn", GroovyScriptListPage.this);
				int scriptIndex = cellItem.findParent(LoopItem.class).getIndex();
				var link = newEditLink("link", scriptIndex);
				link.add(new Label("label", rowModel.getObject().getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});
		columns.add(new AbstractColumn<>(Model.of(_T("Can Be Used By Jobs"))) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				cellItem.add(new Label(componentId, _T(TextUtils.getDisplayValue(rowModel.getObject().isCanBeUsedByBuildJobs()))));
			}
		});
		columns.add(new AbstractColumn<>(Model.of(_T("Job Authorization"))) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				var script = rowModel.getObject();
				if (script.isCanBeUsedByBuildJobs()) {
					if (script.getAuthorization() != null)
						cellItem.add(new Label(componentId, script.getAuthorization()));
					else
						cellItem.add(new Label(componentId, "<i>" + _T("Any job") + "</i>").setEscapeModelStrings(false));
				} else {
					cellItem.add(new Label(componentId, "<i>" + _T("N/A") + "</i>").setEscapeModelStrings(false));
				}
			}
		});
		
		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<GroovyScript>> cellItem, String componentId, IModel<GroovyScript> rowModel) {
				int scriptIndex = cellItem.findParent(LoopItem.class).getIndex();

				Fragment fragment = new Fragment(componentId, "actionColumnFrag", GroovyScriptListPage.this);
				fragment.add(newEditLink("edit", scriptIndex));
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this script?")));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						var script = scripts.remove(scriptIndex);
						var oldAuditContent = VersionedXmlDoc.fromBean(script).toXML();
						OneDev.getInstance(SettingService.class).saveGroovyScripts(scripts);
						auditService.audit(null, "deleted groovy script \"" + script.getName() + "\"", oldAuditContent, null);
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
		
		add(scriptsTable = new DataTable<>("scripts", columns, dataProvider, Integer.MAX_VALUE));
		scriptsTable.addTopToolbar(new HeadersToolbar<Void>(scriptsTable, null));
		scriptsTable.addBottomToolbar(new NoRecordsToolbar(scriptsTable));
		scriptsTable.add(new NoRecordsBehavior());
		scriptsTable.setOutputMarkupId(true);
		
		scriptsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				CollectionUtils.move(scripts, from.getItemIndex(), to.getItemIndex());
				OneDev.getInstance(SettingService.class).saveGroovyScripts(scripts);
				target.add(scriptsTable);
			}
			
		}.sortable("tbody"));
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Groovy Scripts"));
	}
	
}
