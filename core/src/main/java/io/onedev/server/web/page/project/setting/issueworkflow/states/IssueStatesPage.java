package io.onedev.server.web.page.project.setting.issueworkflow.states;

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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.base.Preconditions;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.SideFloating;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class IssueStatesPage extends IssueWorkflowPage {

	private DataTable<StateSpec, Void> statesTable;
	
	public IssueStatesPage(PageParameters params) {
		super(params);
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
					protected IssueWorkflow getWorkflow() {
						return IssueStatesPage.this.getWorkflow();
					}

				};
			}
			
		});
		
		List<IColumn<StateSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				Fragment fragment = new Fragment(componentId, "nameColumnFrag", IssueStatesPage.this);
				StateSpec state = rowModel.getObject();
				int index = getWorkflow().getStateSpecIndex(state.getName());
				Preconditions.checkState(index != -1);
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						newStateDetail(target, index);
					}
					
				};
				link.add(new Label("label", state.getName()));
				link.add(new WebMarkupContainer("initial").setVisible(index == 0));
				fragment.add(link);
				cellItem.add(fragment);
			}
		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Color")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				Fragment fragment = new Fragment(componentId, "otherColumnFrag", IssueStatesPage.this);
				StateSpec state = rowModel.getObject();
				int index = getWorkflow().getStateSpecIndex(state.getName());				
				Preconditions.checkState(index != -1);
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						newStateDetail(target, index);
					}
					
				};
				link.add(new Label("label").add(AttributeAppender.append("style", "background: " + state.getColor() + ";")));
				fragment.add(AttributeAppender.append("class", "color"));
				fragment.add(link);
				cellItem.add(fragment);
			}
			
		});		
		
		columns.add(new AbstractColumn<StateSpec, Void>(Model.of("Description")) {

			@Override
			public void populateItem(Item<ICellPopulator<StateSpec>> cellItem, String componentId, IModel<StateSpec> rowModel) {
				Fragment fragment = new Fragment(componentId, "otherColumnFrag", IssueStatesPage.this);
				StateSpec state = rowModel.getObject();
				int index = getWorkflow().getStateSpecIndex(state.getName());				
				Preconditions.checkState(index != -1);
				AjaxLink<Void> link = new AjaxLink<Void>("link") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						newStateDetail(target, index);
					}
					
				};
				link.add(new Label("label", state.getDescription()));
				fragment.add(link);
				cellItem.add(fragment);
			}
			
		});		
		
		IDataProvider<StateSpec> dataProvider = new ListDataProvider<StateSpec>() {

			@Override
			protected List<StateSpec> getData() {
				return getWorkflow().getStateSpecs();
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
						Collections.swap(getWorkflow().getStateSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getWorkflow().getStateSpecs(), fromIndex-i, fromIndex-i-1);
				}
				
				getProject().setIssueWorkflow(getWorkflow());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				target.add(statesTable);
			}
			
		}.sortable("tbody").handle(".drag-handle").helperClass("sort-helper"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueStatesResourceReference()));
	}

	private void newStateDetail(AjaxRequestTarget target, int index) {
		new SideFloating(target, SideFloating.Placement.RIGHT) {

			private StateSpec getState() {
				return getWorkflow().getStateSpecs().get(index);
				
			}
			@Override
			protected String getTitle() {
				return getState().getName();
			}

			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(AttributeAppender.append("class", "state-spec"));
			}

			@Override
			protected Component newBody(String id) {
				SideFloating sideFloating = this;
				Fragment fragment = new Fragment(id, "viewStateFrag", IssueStatesPage.this);
				fragment.add(BeanContext.viewBean("viewer", getState(), Sets.newHashSet("name")));
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
							protected IssueWorkflow getWorkflow() {
								return IssueStatesPage.this.getWorkflow();
							}

						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this state?"));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						String stateName = getState().getName();
						getWorkflow().getStateSpecs().remove(index);
						getWorkflow().onDeleteState(stateName);
						getWorkflow().setReconciled(false);
						getProject().setIssueWorkflow(getWorkflow());
						OneDev.getInstance(ProjectManager.class).save(getProject());
						target.add(statesTable);
						close();
					}
					
				});
				
				fragment.add(new NotificationPanel("feedback", fragment));
				fragment.setOutputMarkupId(true);
				
				return fragment;
			}

		};		
	}
	
}
