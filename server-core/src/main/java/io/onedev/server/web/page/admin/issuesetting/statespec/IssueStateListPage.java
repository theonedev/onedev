package io.onedev.server.web.page.admin.issuesetting.statespec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
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

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.StateSpec;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChanged;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

@SuppressWarnings("serial")
public class IssueStateListPage extends IssueSettingPage {

	public IssueStateListPage(PageParameters params) {
		super(params);
	}

	private DataTable<StateSpec, Void> statesTable;
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new StateEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(statesTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected GlobalIssueSetting getSetting() {
						return IssueStateListPage.this.getSetting();
					}

				};
			}
			
		});
		
		List<IColumn<StateSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				StateSpec state = rowModel.getObject();
				String html = String.format("<svg class='icon drag-indicator'><use xlink:href='%s'/></svg> %s", 
						SpriteImage.getVersionedHref("grip"), HtmlEscape.escapeHtml5(state.getName()));

				if (cellItem.findParent(LoopItem.class).getIndex() == 0)
					html += " <span class='badge badge-light-info badge-sm ml-2'>Initial</span>";
				cellItem.add(new Label(componentId, html).setEscapeModelStrings(false));
			}
		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Color")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				StateSpec state = rowModel.getObject();
				cellItem.add(new Label(componentId).add(AttributeAppender.append("style", "background: " + state.getColor() + ";")));
				cellItem.add(AttributeAppender.append("class", "color"));
			}
			
		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				StateSpec state = rowModel.getObject();
				String description = state.getDescription();
				if (description != null)
					cellItem.add(new Label(componentId, description));
				else
					cellItem.add(new Label(componentId, "<i>No description</i>").setEscapeModelStrings(false));
			}
			
			@Override
			public String getCssClass() {
				return "d-none d-lg-table-cell";
			}

		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Actions")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				int stateIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "stateActionsFrag", IssueStateListPage.this);
				fragment.add(new ModalLink("edit") {

					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new StateEditPanel(id, stateIndex) {

							@Override
							protected void onSave(AjaxRequestTarget target) {
								target.add(statesTable);
								modal.close();
							}

							@Override
							protected void onCancel(AjaxRequestTarget target) {
								modal.close();
							}

							@Override
							protected GlobalIssueSetting getSetting() {
								return IssueStateListPage.this.getSetting();
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this state?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getSetting().getStateSpecs().remove(stateIndex);
						getSetting().setReconciled(false);
						OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
						target.add(statesTable);
						send(getPage(), Broadcast.BREADTH, new WorkflowChanged(target));
					}
					
				});
				cellItem.add(fragment);
			}

		});		
		
		IDataProvider<StateSpec> dataProvider = new ListDataProvider<StateSpec>() {

			@Override
			protected List<StateSpec> getData() {
				return getSetting().getStateSpecs();
			}

		};
		
		add(statesTable = new DataTable<StateSpec, Void>("issueStates", columns, dataProvider, Integer.MAX_VALUE));
		statesTable.addTopToolbar(new HeadersToolbar<Void>(statesTable, null));
		statesTable.addBottomToolbar(new NoRecordsToolbar(statesTable));
		statesTable.add(new NoRecordsBehavior());
		statesTable.setOutputMarkupId(true);
		
		statesTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(getSetting().getStateSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getSetting().getStateSpecs(), fromIndex-i, fromIndex-i-1);
				}
				
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				target.add(statesTable);
			}
			
		}.sortable("tbody"));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Issue States");
	}
	
}
