package io.onedev.server.web.page.admin.issuesetting;

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
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.BoardSpec;
import io.onedev.server.issue.fieldspec.FieldSpec;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.issue.board.BoardEditPanel;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.SideFloating;

@SuppressWarnings("serial")
public class DefaultBoardListPage extends IssueSettingPage {

	private DataTable<BoardSpec, Void> boardsTable;
	
	public DefaultBoardListPage(PageParameters params) {
		super(params);
	}

	private int getBoardSpecIndex(String boardName) {
		for (int i=0; i<getSetting().getBoardSpecs().size(); i++) {
			if (getSetting().getBoardSpecs().get(i).getName().equals(boardName))
				return i;
		}
		return -1;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new BoardEditPanel(id, getSetting().getBoardSpecs(), -1) {

					@Override
					protected void onBoardSaved(AjaxRequestTarget target, BoardSpec board) {
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
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				BoardSpec board = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, board) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, "<span class=\"drag-indicator fa fa-reorder\"></span> " 
								+ HtmlEscape.escapeHtml5(board.getName())).setEscapeModelStrings(false);
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("Identify Field")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				BoardSpec board = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, board) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, board.getIdentifyField());
					}
					
				});
			}
			
		});		
		
		columns.add(new AbstractColumn<BoardSpec, Void>(Model.of("Columns")) {

			@Override
			public void populateItem(Item<ICellPopulator<BoardSpec>> cellItem, String componentId, IModel<BoardSpec> rowModel) {
				BoardSpec board = rowModel.getObject();
				List<String> columnsForDisplay = new ArrayList<>();
				for (String column: rowModel.getObject().getColumns()) {
					if (column == null) {
						GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
						FieldSpec field = issueSetting.getFieldSpec(board.getIdentifyField());
						if (field != null)
							columnsForDisplay.add("<i>" + HtmlEscape.escapeHtml5(field.getNameOfEmptyValue()) + "</i>");
					} else {
						columnsForDisplay.add(HtmlEscape.escapeHtml5(column));
					}
				}
				cellItem.add(new ColumnFragment(componentId, board) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, StringUtils.join(columnsForDisplay)).setEscapeModelStrings(false);
					}
					
				});
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
		boardsTable.setOutputMarkupId(true);
		
		boardsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(getSetting().getBoardSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getSetting().getBoardSpecs(), fromIndex-i, fromIndex-i-1);
				}
				
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				target.add(boardsTable);
			}
			
		}.sortable("tbody"));
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final BoardSpec board;
		
		public ColumnFragment(String id, BoardSpec board) {
			super(id, "columnFrag", DefaultBoardListPage.this);
			this.board = board;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			
			int index = getBoardSpecIndex(board.getName());				
			Preconditions.checkState(index != -1);
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						private BoardSpec getBoard() {
							return getSetting().getBoardSpecs().get(index);
							
						}
						@Override
						protected String getTitle() {
							return getBoard().getName();
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "board-spec def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewBoardFrag", DefaultBoardListPage.this);

							board.populateEditColumns();
							
							fragment.add(BeanContext.view("viewer", getBoard(), Sets.newHashSet("name"), true));
							fragment.add(new ModalLink("edit") {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									sideFloating.close();
									return new BoardEditPanel(id, getSetting().getBoardSpecs(), index) {

										@Override
										protected void onBoardSaved(AjaxRequestTarget target, BoardSpec board) {
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
							fragment.add(new AjaxLink<Void>("delete") {

								@Override
								protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
									super.updateAjaxAttributes(attributes);
									attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this board?"));
								}

								@Override
								public void onClick(AjaxRequestTarget target) {
									getSetting().getBoardSpecs().remove(index);
									OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
									target.add(boardsTable);
									close();
								}
								
							});
							
							fragment.setOutputMarkupId(true);
							
							return fragment;
						}

					};	
				}
				
			};
			link.add(newLabel("label"));
			add(link);
		}
		
		protected abstract Component newLabel(String componentId);
	}
	
}
