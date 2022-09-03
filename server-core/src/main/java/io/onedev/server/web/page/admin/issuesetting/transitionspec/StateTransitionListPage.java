package io.onedev.server.web.page.admin.issuesetting.transitionspec;

import java.util.ArrayList;
import java.util.Collections;
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
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.issue.TransitionSpec;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.NoRecordsBehavior;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.modal.ModalLink;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.admin.issuesetting.IssueSettingPage;

@SuppressWarnings("serial")
public class StateTransitionListPage extends IssueSettingPage {

	private DataTable<TransitionSpec, Void> transitionsTable;
	
	public StateTransitionListPage(PageParameters params) {
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
					protected GlobalIssueSetting getSetting() {
						return StateTransitionListPage.this.getSetting();
					}

				};
			}
			
		});
		
		List<IColumn<TransitionSpec, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
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
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				TransitionSpec transition = rowModel.getObject();
				Fragment fragment = new Fragment(componentId, "descriptionFrag", StateTransitionListPage.this);
				if (transition.getFromStates().size() > 1)
					fragment.add(new Label("fromStates", "[" + StringUtils.join(transition.getFromStates(), ",") + "]"));
				else
					fragment.add(new Label("fromStates", transition.getFromStates().iterator().next()));
				
				fragment.add(new Label("toState", transition.getToState()));

				fragment.add(new Label("when", "When " + transition.getTrigger().getDescription()));

				if (transition.getTrigger().getIssueQuery() != null)
					fragment.add(new Label("applicable", "For issues matching: " + transition.getTrigger().getIssueQuery()));
				else
					fragment.add(new Label("applicable", "For all issues"));
				
				cellItem.add(fragment);
			}
			
		});		
		
		columns.add(new AbstractColumn<TransitionSpec, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<TransitionSpec>> cellItem, String componentId, IModel<TransitionSpec> rowModel) {
				int transitionIndex = cellItem.findParent(LoopItem.class).getIndex();
				Fragment fragment = new Fragment(componentId, "actionColumnFrag", StateTransitionListPage.this);
				fragment.add(new ModalLink("edit") {
	
					@Override
					protected Component newContent(String id, ModalPanel modal) {
						return new TransitionEditPanel(id, transitionIndex) {
	
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
							protected GlobalIssueSetting getSetting() {
								return StateTransitionListPage.this.getSetting();
							}
	
						};
					}
					
				});
				fragment.add(new AjaxLink<Void>("delete") {
	
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.getAjaxCallListeners().add(new ConfirmClickListener("Do you really want to delete this transition?"));
					}
	
					@Override
					public void onClick(AjaxRequestTarget target) {
						getSetting().getTransitionSpecs().remove(transitionIndex);
						OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
						target.add(transitionsTable);
					}
					
				});
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions text-nowrap";
			}
			
		});		
		
		IDataProvider<TransitionSpec> dataProvider = new ListDataProvider<TransitionSpec>() {

			@Override
			protected List<TransitionSpec> getData() {
				return getSetting().getTransitionSpecs();			
			}

		};
		
		add(transitionsTable = new DataTable<TransitionSpec, Void>("stateTransitions", columns, dataProvider, Integer.MAX_VALUE));
		transitionsTable.addTopToolbar(new HeadersToolbar<Void>(transitionsTable, null));
		transitionsTable.addBottomToolbar(new NoRecordsToolbar(transitionsTable));
		transitionsTable.add(new NoRecordsBehavior());
		
		transitionsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				int fromIndex = from.getItemIndex();
				int toIndex = to.getItemIndex();
				if (fromIndex < toIndex) {
					for (int i=0; i<toIndex-fromIndex; i++) 
						Collections.swap(getSetting().getTransitionSpecs(), fromIndex+i, fromIndex+i+1);
				} else {
					for (int i=0; i<fromIndex-toIndex; i++) 
						Collections.swap(getSetting().getTransitionSpecs(), fromIndex-i, fromIndex-i-1);
				}
				OneDev.getInstance(SettingManager.class).saveIssueSetting(getSetting());
				target.add(transitionsTable);
			}
			
		}.sortable("tbody"));
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "<span class='text-truncate'>Issue State Transitions</span>").setEscapeModelStrings(false);
	}
	
}
