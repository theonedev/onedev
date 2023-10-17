package io.onedev.server.web.page.admin.issuesetting.linkspec;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.LinkSpecManager;
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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class LinkSpecListPage extends IssueSettingPage {

	public LinkSpecListPage(PageParameters params) {
		super(params);
	}

	private DataTable<LinkSpec, Void> linksTable;
	
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
		
		columns.add(new AbstractColumn<LinkSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<LinkSpec>> cellItem, String componentId, IModel<LinkSpec> rowModel) {
				LinkSpec link = rowModel.getObject();
				cellItem.add(new Label(componentId, link.getName()));
			}
		});		
		
		columns.add(new AbstractColumn<LinkSpec, Void>(Model.of("Name On the Other Side")) {

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
				Fragment fragment = new Fragment(componentId, "linkActionsFrag", LinkSpecListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new LinkSpecEditPanel(id, rowModel) {

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
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this link?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getLinkSpecManager().delete(rowModel.getObject());
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
				return getLinkSpecManager().queryAndSort();
			}

			@Override
			public IModel<LinkSpec> model(LinkSpec object) {
				Long id = object.getId();
				return new LoadableDetachableModel<LinkSpec>() {

					@Override
					protected LinkSpec load() {
						return getLinkSpecManager().load(id);
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
				List<LinkSpec> links = getLinkSpecManager().queryAndSort();
				CollectionUtils.move(links, from.getItemIndex(), to.getItemIndex());
				getLinkSpecManager().updateOrders(links);
				
				target.add(linksTable);
			}
			
		}.sortable("tbody"));
	}

	private LinkSpecManager getLinkSpecManager() {
		return OneDev.getInstance(LinkSpecManager.class);
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Issue Links");
	}
	
}
