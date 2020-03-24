package io.onedev.server.web.component.issue.statetransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.unbescape.html.HtmlEscape;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.issue.TransitionSpec;
import io.onedev.server.web.ajaxlistener.ConfirmListener;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.SideFloating;
import io.onedev.server.web.util.ConfirmOnClick;

@SuppressWarnings("serial")
public abstract class StateTransitionListPanel extends Panel {

	private final List<TransitionSpec> transitions;
	
	public StateTransitionListPanel(String componentId, List<TransitionSpec> transitions) {
		super(componentId);
		this.transitions = transitions;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ModalLink("addNew") {

			@Override
			protected Component newContent(String id, ModalPanel modal) {
				return new TransitionEditPanel(id, transitions, -1) {

					@Override
					protected void onChanged(AjaxRequestTarget target) {
						StateTransitionListPanel.this.onChanged(target);
						target.add(StateTransitionListPanel.this);
						modal.close();
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						modal.close();
					}

				};
			}
			
		});
		
		String message = "This will discard all project specific transitions, do you want to continue?";
		add(new Link<Void>("useDefault") {

			@Override
			public void onClick() {
				getUseDefaultListener().onUseDefault();
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getUseDefaultListener() != null);
			}
			
		}.add(new ConfirmOnClick(message)));
		
		List<IColumn<TransitionSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("From States")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				TransitionSpec transition = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, transition) {

					@Override
					protected Component newLabel(String componentId) {
						String escaped = HtmlEscape.escapeHtml5(StringUtils.join(transition.getFromStates())); 
						return new Label(componentId, "<span class='drag-indicator fa fa-reorder'></span> " + escaped)
								.setEscapeModelStrings(false);
					}
					
				});
			}
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("To State")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				TransitionSpec transition = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, transition) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, transition.getToState());
					}
					
				});
			}
			
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("Do Transition When")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				TransitionSpec transition = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, transition) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, transition.getTrigger().getDescription());
					}
					
				});
			}
			
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("Applicable Issues")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				TransitionSpec transition = rowModel.getObject();
				cellItem.add(new ColumnFragment(componentId, transition) {

					@Override
					protected Component newLabel(String componentId) {
						if (transition.getTrigger().getIssueQuery() != null)
							return new Label(componentId, transition.getTrigger().getIssueQuery());
						else
							return new Label(componentId, "<i>All</i>").setEscapeModelStrings(false);
					}
					
				});
			}
			
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				cellItem.add(new ColumnFragment(componentId, rowModel.getObject()) {

					@Override
					protected Component newLabel(String componentId) {
						return new Label(componentId, "<i class='fa fa-ellipsis-h'></i>").setEscapeModelStrings(false);
					}
					
				});
			}

			@Override
			public String getCssClass() {
				return "ellipsis";
			}
			
		});		
		
		IDataProvider<TransitionSpec> dataProvider = new ListDataProvider<TransitionSpec>() {

			@Override
			protected List<TransitionSpec> getData() {
				return transitions;			
			}

		};
		
		DataTable<TransitionSpec, Void> dataTable;
		add(dataTable = new DataTable<TransitionSpec, Void>("stateTransitions", columns, dataProvider, Integer.MAX_VALUE));
		dataTable.addTopToolbar(new HeadersToolbar<Void>(dataTable, null));
		dataTable.addBottomToolbar(new NoRecordsToolbar(dataTable));
		
		dataTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(transitions, fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(transitions, fromIndex-i, fromIndex-i-1);
				}
				onChanged(target);
				target.add(StateTransitionListPanel.this);
			}
			
		}.sortable("tbody"));
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new StateTransitionListResourceReference()));
	}

	private int getTransitionSpecIndex(TransitionSpec transition) {
		for (int i=0; i<transitions.size(); i++) {
			if (transitions.get(i) == transition)
				return i;
		}
		return -1;
	}
	
	private abstract class ColumnFragment extends Fragment {

		private final int index;
		
		public ColumnFragment(String id, TransitionSpec transition) {
			super(id, "columnFrag", StateTransitionListPanel.this);
			this.index = getTransitionSpecIndex(transition);
			Preconditions.checkState(this.index != -1);
		}
		
		private TransitionSpec getTransition() {
			return transitions.get(index);
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
							add(AttributeAppender.append("class", "transition-spec def-detail"));
						}

						@Override
						protected Component newBody(String id) {
							SideFloating sideFloating = this;
							Fragment fragment = new Fragment(id, "viewTransitionFrag", StateTransitionListPanel.this);
							fragment.add(BeanContext.view("viewer", getTransition(), Sets.newHashSet("name"), true));
							
							fragment.add(new ModalLink("edit") {

								@Override
								protected Component newContent(String id, ModalPanel modal) {
									sideFloating.close();
									return new TransitionEditPanel(id, transitions, index) {

										@Override
										protected void onChanged(AjaxRequestTarget target) {
											StateTransitionListPanel.this.onChanged(target);
											target.add(StateTransitionListPanel.this);
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
									attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this state?"));
								}

								@Override
								public void onClick(AjaxRequestTarget target) {
									transitions.remove(index);
									onChanged(target);
									target.add(StateTransitionListPanel.this);
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
	
	protected abstract void onChanged(AjaxRequestTarget target);
	
	@Nullable
	protected abstract UseDefaultListener getUseDefaultListener();
	
}
