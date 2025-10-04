package io.onedev.server.web.page.admin.issuesetting.defaultboard;

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

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.service.SettingService;
import io.onedev.server.model.support.issue.BoardSpec;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.issue.board.BoardEditPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

public class DefaultBoardListPage extends IssueSettingPage {

	private DataTable<BoardSpec, Void> boardsTable;
	
	public DefaultBoardListPage(PageParameters params) {
		super(params);
	}

	private WebMarkupContainer newEditLink(String componentId, int boardIndex) {
		return new ModalLink(componentId) {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getSetting().getBoardSpecs().get(boardIndex)).toXML();
				return new BoardEditPanel(id, getSetting().getBoardSpecs(), boardIndex) {

					@Override
					protected void onSave(AjaxRequestTarget target, BoardSpec board) {
						var newAuditContent = VersionedXmlDoc.fromBean(board).toXML();
						OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
						auditService.audit(null, "changed default issue board \"" + board.getName() + "\"", oldAuditContent, newAuditContent);
						target.add(boardsTable);
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
				return new BoardEditPanel(id, getSetting().getBoardSpecs(), -1) {

					@Override
					protected void onSave(AjaxRequestTarget target, BoardSpec board) {
						target.add(boardsTable);
						modal.close();
						var newAuditContent = VersionedXmlDoc.fromBean(board).toXML();
						OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
						auditService.audit(null, "added default issue board \"" + board.getName() + "\"", null, newAuditContent);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
		});
		
		List<IColumn<BoardSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
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

		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of(_T("Name"))) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				var fragment = new Fragment(componentId, "nameColumnFrag", DefaultBoardListPage.this);
				int boardIndex = cellItem.findParent(LoopItem.class).getIndex();
				var link = newEditLink("link", boardIndex);
				link.add(new Label("label", rowModel.getObject().getName()));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of(_T("Columns"))) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				BoardSpec board = rowModel.getObject();
				cellItem.add(new Label(componentId, StringUtils.join(board.getDisplayColumns())));
			}
			
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of(_T("Identify Field"))) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				BoardSpec board = rowModel.getObject();
				cellItem.add(new Label(componentId, board.getIdentifyField()));
			}

			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}
			
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				int boardIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", DefaultBoardListPage.this);

				fragment.add(newEditLink("edit", boardIndex));
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this board?")));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						var board = getSetting().getBoardSpecs().remove(boardIndex);
						var oldAuditContent = VersionedXmlDoc.fromBean(board).toXML();
						OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
						auditService.audit(null, "deleted default issue board \"" + board.getName() + "\"", oldAuditContent, null);
						target.add(boardsTable);
					}
					
				});
				cellItem.add(fragment);
			}
			
			@Override
			public String getCssClass() {
				return "actions text-nowrap";
			}
			
		});		
		
		IDataProvider<BoardSpec> dataProvider = new ListDataProvider<BoardSpec>() {

			@Override
			protected List<BoardSpec> getData() {
				return getSetting().getBoardSpecs();
			}

		};
		
		add(boardsTable = new DataTable<BoardSpec, Void>("issueBoards", columns, dataProvider, Integer.MAX_VALUE));
		boardsTable.addTopToolbar(new HeadersToolbar<Void>(boardsTable, null));
		boardsTable.addBottomToolbar(new NoRecordsToolbar(boardsTable));
		boardsTable.add(new NoRecordsBehavior());
		boardsTable.setOutputMarkupId(true);
		
		boardsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				var oldAuditContent = VersionedXmlDoc.fromBean(getSetting().getBoardSpecs()).toXML();
				CollectionUtils.move(getSetting().getBoardSpecs(), from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(getSetting().getBoardSpecs()).toXML();
				OneDev.getInstance(SettingService.class).saveIssueSetting(getSetting());
				auditService.audit(null, "changed order of default issue boards", oldAuditContent, newAuditContent);
				target.add(boardsTable);
			}
			
		}.sortable("tbody"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Default Issue Boards"));
	}
	
}
