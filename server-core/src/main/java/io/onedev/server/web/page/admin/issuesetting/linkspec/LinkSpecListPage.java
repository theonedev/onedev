package io.onedev.server.web.page.admin.issuesetting.linkspec;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.LinkSpecService;
import io.onedev.server.model.LinkSpec;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class LinkSpecListPage extends IssueSettingPage {

	public LinkSpecListPage(PageParameters params) {
		super(params);
	}

	private DataTable<LinkSpec, Void> linksTable;
	
	private WebMarkupContainer newEditLink(String componentId, IModel<LinkSpec> model) {
		return new ModalLink(componentId) {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new LinkSpecEditPanel(id, model) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(linksTable);
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
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new LinkSpecEditPanel(id, Model.of(new LinkSpec())) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(linksTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
		});
		
		List<IColumn<LinkSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<LinkSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<LinkSpec>> cellItem, String componentId, IModel<LinkSpec> rowModel) {
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
		
		columns.add(new AbstractColumn<LinkSpec, Void>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<LinkSpec>> cellItem, String componentId, IModel<LinkSpec> rowModel) {
				var fragment = new Fragment(componentId, "nameColumnFrag", LinkSpecListPage.this);
				var link = newEditLink("link", rowModel);
				link.add(new Label("label", rowModel.getObject().getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});		
		
		columns.add(new AbstractColumn<LinkSpec, Void>(Model.of(_T("Name On the Other Side"))) {

			@Override
			public void populateItem(Item<ICellPopulator<LinkSpec>> cellItem, String componentId, IModel<LinkSpec> rowModel) {
				LinkSpec link = rowModel.getObject();
				if (link.getOpposite() != null)
					cellItem.add(new Label(componentId, link.getOpposite().getName()));
				else
					cellItem.add(new Label(componentId, link.getName()));
			}
			
		});		
		
		columns.add(new AbstractColumn<LinkSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<LinkSpec>> cellItem, String componentId, IModel<LinkSpec> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", LinkSpecListPage.this);
				fragment.add(newEditLink("edit", rowModel));
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this link?")));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						var link = rowModel.getObject();
						getLinkSpecService().delete(link);
						var oldAuditContent = VersionedXmlDoc.fromBean(link).toXML();
						auditService.audit(null, "deleted issue link spec \"" + link.getDisplayName() + "\"", oldAuditContent, null);
						target.add(linksTable);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-nowrap";
			}
			
		});		
		
		IDataProvider<LinkSpec> dataProvider = new ListDataProvider<LinkSpec>() {

			@Override
			protected List<LinkSpec> getData() {
				return getLinkSpecService().queryAndSort();
			}

			@Override
			public IModel<LinkSpec> model(LinkSpec object) {
				Long id = object.getId();
				return new LoadableDetachableModel<LinkSpec>() {

					@Override
					protected LinkSpec load() {
						return getLinkSpecService().load(id);
					}
					
				};
			}
			
		};
		
		add(linksTable = new DataTable<LinkSpec, Void>("links", columns, dataProvider, Integer.MAX_VALUE));
		linksTable.addTopToolbar(new HeadersToolbar<Void>(linksTable, null));
		linksTable.addBottomToolbar(new NoRecordsToolbar(linksTable));
		linksTable.add(new NoRecordsBehavior());
		linksTable.setOutputMarkupId(true);
		
		linksTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				List<LinkSpec> links = getLinkSpecService().queryAndSort();
				var oldAuditContent = VersionedXmlDoc.fromBean(links).toXML();
				CollectionUtils.move(links, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(links).toXML();
				getLinkSpecService().updateOrders(links);
				auditService.audit(null, "changed order of issue link specs", oldAuditContent, newAuditContent);
				target.add(linksTable);
			}
			
		}.sortable("tbody"));
	}

	private LinkSpecService getLinkSpecService() {
		return OneDev.getInstance(LinkSpecService.class);
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Issue Links"));
	}
	
}
