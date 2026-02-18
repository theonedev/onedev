package io.onedev.server.web.page.project.setting.webhook;

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
import io.onedev.server.model.Project;
import io.onedev.server.model.support.WebHook;
import io.onedev.server.service.ProjectService;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.setting.ProjectSettingPage;

public class WebHooksPage extends ProjectSettingPage {

	private DataTable<WebHook, Void> hooksTable;

	public WebHooksPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new WebHookEditPanel(id, -1) {

					@Override
					protected Project getProject() {
						return WebHooksPage.this.getProject();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(hooksTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}

		});

		List<IColumn<WebHook, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<WebHook, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<WebHook>> cellItem, String componentId, IModel<WebHook> rowModel) {
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

		columns.add(new AbstractColumn<WebHook, Void>(Model.of(_T("Post URL"))) {

			@Override
			public void populateItem(Item<ICellPopulator<WebHook>> cellItem, String componentId, IModel<WebHook> rowModel) {
				int hookIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "urlColumnFrag", WebHooksPage.this);
				var link = newEditLink("link", hookIndex);
				link.add(new Label("label", rowModel.getObject().getPostUrl()));
				fragment.add(link);
				cellItem.add(fragment);
			}

		});

		columns.add(new AbstractColumn<WebHook, Void>(Model.of(_T("Events"))) {

			@Override
			public void populateItem(Item<ICellPopulator<WebHook>> cellItem, String componentId, IModel<WebHook> rowModel) {
				var eventTypes = rowModel.getObject().getEventTypes();
				cellItem.add(new Label(componentId, eventTypes.size()));
			}

		});

		columns.add(new AbstractColumn<WebHook, Void>(Model.of(_T("Custom Headers"))) {

			@Override
			public void populateItem(Item<ICellPopulator<WebHook>> cellItem, String componentId, IModel<WebHook> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getHeaders().size()));
			}

		});

		columns.add(new AbstractColumn<WebHook, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<WebHook>> cellItem, String componentId, IModel<WebHook> rowModel) {
				int hookIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", WebHooksPage.this);
				fragment.add(newEditLink("edit", hookIndex));
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this web hook?")));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						var hook = getProject().getWebHooks().remove(hookIndex);
						var oldAuditContent = VersionedXmlDoc.fromBean(hook).toXML();
						OneDev.getInstance(ProjectService.class).update(getProject());
						auditService.audit(getProject(), "deleted web hook \"" + hook.getPostUrl() + "\"", oldAuditContent, null);
						target.add(hooksTable);
					}

				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-nowrap";
			}

		});

		IDataProvider<WebHook> dataProvider = new ListDataProvider<WebHook>() {

			@Override
			protected List<WebHook> getData() {
				return getProject().getWebHooks();
			}

		};

		add(hooksTable = new DataTable<WebHook, Void>("webHooks", columns, dataProvider, Integer.MAX_VALUE));
		hooksTable.addTopToolbar(new HeadersToolbar<Void>(hooksTable, null));
		hooksTable.addBottomToolbar(new NoRecordsToolbar(hooksTable));
		hooksTable.add(new NoRecordsBehavior());
		hooksTable.setOutputMarkupId(true);

		hooksTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getProject().getWebHooks()).toXML();
				CollectionUtils.move(getProject().getWebHooks(), from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(getProject().getWebHooks()).toXML();
				OneDev.getInstance(ProjectService.class).update(getProject());
				auditService.audit(getProject(), "changed order of web hooks", oldAuditContent, newAuditContent);
				target.add(hooksTable);
			}

		}.sortable("tbody"));
	}

	private WebMarkupContainer newEditLink(String componentId, int hookIndex) {
		return new ModalLink(componentId) {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new WebHookEditPanel(id, hookIndex) {

					@Override
					protected Project getProject() {
						return WebHooksPage.this.getProject();
					}

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(hooksTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}

		};
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Web Hooks"));
	}

}
