package io.onedev.server.web.page.admin.issuesetting.defaultboard;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
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
public class DefaultBoardListPage extends IssueSettingPage {

	private DataTable<BoardSpec, Void> boardsTable;
	
	public DefaultBoardListPage(PageParameters params) {
		super(params);
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
						OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
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

		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("Columns")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				BoardSpec board = rowModel.getObject();
				cellItem.add(new Label(componentId, StringUtils.join(board.getDisplayColumns())));
			}
			
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("Identify Field")) {

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

				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new BoardEditPanel(id, getSetting().getBoardSpecs(), boardIndex) {

							@Override
							protected void onSave(AjaxRequestTarget target, BoardSpec board) {
								OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
								target.add(boardsTable);
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
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this board?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getSetting().getBoardSpecs().remove(boardIndex);
						OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
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
				CollectionUtils.move(getSetting().getBoardSpecs(), from.getItemIndex(), to.getItemIndex());
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				target.add(boardsTable);
			}
			
		}.sortable("tbody"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Default Issue Boards");
	}
	
}
