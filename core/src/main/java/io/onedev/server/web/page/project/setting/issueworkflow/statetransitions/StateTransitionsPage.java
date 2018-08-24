package io.onedev.server.web.page.project.setting.issueworkflow.statetransitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import io.onedev.server.model.support.issue.workflow.TransitionSpec;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.page.layout.SideFloating;
import io.onedev.server.web.page.project.setting.issueworkflow.IssueWorkflowPage;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;
import io.onedev.utils.StringUtils;
import jersey.repackaged.com.google.common.collect.Sets;

@SuppressWarnings("serial")
public class StateTransitionsPage extends IssueWorkflowPage {

	private DataTable<TransitionSpec, Void> transitionsTable;
	
	public StateTransitionsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new TransitionEditPanel(id, -1) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						target.add(transitionsTable);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

					@Override
					protected IssueWorkflow getWorkflow() {
						return StateTransitionsPage.this.getWorkflow();
					}

				};
			}
			
		});
		
		List<IColumn<TransitionSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("From States")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				String label = StringUtils.join(rowModel.getObject().getFromStates());
				cellItem.add(new ColumnFragment(componentId, rowModel.getObject(), label));
			}
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("To State")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel.getObject(), rowModel.getObject().getToState()));
			}
			
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("Do Transition When")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				String label = EditableUtils.getDisplayName(rowModel.getObject().getTrigger().getClass());
				cellItem.add(new ColumnFragment(componentId, rowModel.getObject(), label));
			}
			
		});		
		
		IDataProvider<TransitionSpec> dataProvider = new ListDataProvider<TransitionSpec>() {

			@Override
			protected List<TransitionSpec> getData() {
				Collections.sort(getWorkflow().getTransitionSpecs(), new Comparator<TransitionSpec>() {

					@Override
					public int compare(TransitionSpec transition1, TransitionSpec transition2) {
						int fromStateIndex1 = getWorkflow().getStateSpecIndex(transition1.getFromStates().get(0));
						int fromStateIndex2 = getWorkflow().getStateSpecIndex(transition2.getFromStates().get(0));
						int toStateIndex1 = getWorkflow().getStateSpecIndex(transition1.getToState());
						int toStateIndex2 = getWorkflow().getStateSpecIndex(transition2.getToState());
						if (fromStateIndex1 != fromStateIndex2)
							return fromStateIndex1 - fromStateIndex2;
						else
							return toStateIndex1 - toStateIndex2;
					}
					
				});
				return getWorkflow().getTransitionSpecs();
			}

		};
		
		add(transitionsTable = new DataTable<TransitionSpec, Void>("stateTransitions", columns, dataProvider, Integer.MAX_VALUE));
		transitionsTable.addTopToolbar(new HeadersToolbar<Void>(transitionsTable, null));
		transitionsTable.addBottomToolbar(new NoRecordsToolbar(transitionsTable));
		transitionsTable.setOutputMarkupId(true);
		
		transitionsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(getWorkflow().getTransitionSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getWorkflow().getTransitionSpecs(), fromIndex-i, fromIndex-i-1);
				}
				
				getProject().setIssueWorkflow(getWorkflow());
				OneDev.getInstance(ProjectManager.class).save(getProject());
				target.add(transitionsTable);
			}
			
		}.sortable("tbody").handle(".drag-handle").helperClass("sort-helper"));
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new StateTransitionsResourceReference()));
	}

	private class ColumnFragment extends Fragment {

		private final int index;
		
		private final String label;
		
		public ColumnFragment(String id, TransitionSpec transition, String label) {
			super(id, "columnFrag", StateTransitionsPage.this);
			this.index = getWorkflow().getTransitionSpecIndex(transition);
			Preconditions.checkState(this.index != -1);
			this.label = label;
		}
		
		private TransitionSpec getTransition() {
			return getWorkflow().getTransitionSpecs().get(index);
		}

		@Override
		protected void onInitialize() {
			super.onInitialize();
			AjaxLink<Void> link = new AjaxLink<Void>("link") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					new SideFloating(target, SideFloating.Placement.RIGHT) {

						@Override
						protected String getTitle() {
							return "State Transition";
						}

						@Override
						protected void onInitialize() {
							super.onInitialize();
							add(AttributeAppender.append("class", "state-transition"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewTransitionFrag", StateTransitionsPage.this);
							fragment.add(BeanContext.viewBean("viewer", getTransition(), Sets.newHashSet("name"), true));
							fragment.add(new ModalLink("edit") {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									sideFloating.close();
									return new TransitionEditPanel(id, index) {

										@Override
										protected void onSave(AjaxRequestTarget target) {
											target.add(transitionsTable);
											modal.close();
										}

										@Override
										protected void onCancel(AjaxRequestTarget target) {
											modal.close();
										}

										@Override
										protected IssueWorkflow getWorkflow() {
											return StateTransitionsPage.this.getWorkflow();
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
									getWorkflow().getTransitionSpecs().remove(index);
									getProject().setIssueWorkflow(getWorkflow());
									OneDev.getInstance(ProjectManager.class).save(getProject());
									target.add(transitionsTable);
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
			if (label != null)
				link.add(new Label("label", label));
			else
				link.add(new Label("label", "&nbsp;").setEscapeModelStrings(false));
			add(link);
		}
		
	}
}
