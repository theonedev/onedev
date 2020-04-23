package io.onedev.server.web.page.admin.issuesetting;

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

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.issue.StateSpec;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.issue.workflowreconcile.WorkflowChanged;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.SideFloating;

@SuppressWarnings("serial")
public class IssueStateListPage extends IssueSettingPage {

	public IssueStateListPage(PageParameters params) {
		super(params);
	}

	private DataTable<StateSpec, Void> statesTable;
	
	private int getStateSpecIndex(String stateName) {
		for (int i=0; i<getSetting().getStateSpecs().size(); i++) {
			if (getSetting().getStateSpecs().get(i).getName().equals(stateName))
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
				cellItem.add(new ColumnFragment(componentId, state) {

					@Override
					protected Component newLabel(String componentId) {
						String label = "<span class='drag-indicator fa fa-reorder'></span> " 
								+ HtmlEscape.escapeHtml5(state.getName());

						if (getStateSpecIndex(state.getName()) == 0)
							label += " <span class='label label-default'>Initial</span>";
						return new Label(componentId, label).setEscapeModelStrings(false);
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Color")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				StateSpec state = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, state) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId).add(AttributeAppender.append("style", "background: " + state.getColor() + ";"));
					}
					
				}.add(AttributeAppender.append("class", "color")));
			}
			
		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				StateSpec state = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, state) {

					@Override
					protected Component newLabel(String componentId) {
						String description = state.getDescription();
						if (description != null)
							return new Label(componentId, description);
						else
							return new Label(componentId, "<i>No description</i>").setEscapeModelStrings(false);
					}
					
				});
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
	
	private abstract class ColumnFragment extends Fragment {

		private final StateSpec state;
		
		public ColumnFragment(String id, StateSpec state) {
			super(id, "columnFrag", IssueStateListPage.this);
			this.state = state;
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			int index = getStateSpecIndex(state.getName());				
			Preconditions.checkState(index != -1);
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						private StateSpec getState() {
							return getSetting().getStateSpecs().get(index);
						}
						
						@Override
						protected String getTitle() {
							return getState().getName();
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "state-spec def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewStateFrag", IssueStateListPage.this);
							fragment.add(BeanContext.view("viewer", getState(), Sets.newHashSet("name"), true));
							fragment.add(new ModalLink("edit") {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									sideFloating.close();
									return new StateEditPanel(id, index) {

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
									getSetting().getStateSpecs().remove(index);
									getSetting().setReconciled(false);
									OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
									target.add(statesTable);
									send(getPage(), Broadcast.BREADTH, new WorkflowChanged(target));
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
		
		protected abstract Component newLabel(String componentId);
	}
}
