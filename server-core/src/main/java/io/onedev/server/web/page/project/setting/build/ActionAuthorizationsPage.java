package io.onedev.server.web.page.project.setting.build;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.actionauthorization.ActionAuthorization;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.modal.ModalPanel;

@SuppressWarnings("serial")
public class ActionAuthorizationsPage extends BuildSettingPage {

	private DataTable<ActionAuthorization, Void> authorizationsTable;
	
	public ActionAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new AjaxLink<Void>("addNew") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target) {
					
					@Override
					protected Component newContent(String id) {
						return new ActionAuthorizationEditPanel(id, -1) {

							@Override
							protected Project getProject() {
								return ActionAuthorizationsPage.this.getProject();
							}

							@Override
							protected void onCancelled(AjaxRequestTarget target) {
								close();
							}

							@Override
							protected void onSaved(AjaxRequestTarget target) {
								target.add(authorizationsTable);
								close();
							}
							
						};
					}
					
				};
			}
			
		});
		
		List<IColumn<ActionAuthorization, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("Action")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, 
					IModel<ActionAuthorization> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getActionDescription()));
			}
			
		});
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("Authorized Branches")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, 
					IModel<ActionAuthorization> rowModel) {
				if (rowModel.getObject().getAuthorizedBranches() != null)
					cellItem.add(new Label(componentId, rowModel.getObject().getAuthorizedBranches()));
				else
					cellItem.add(new Label(componentId, "<i>All</i>").setEscapeModelStrings(false));
			}
			
		});
		
		columns.add(new AbstractColumn<ActionAuthorization, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ActionAuthorization>> cellItem, String componentId, 
					IModel<ActionAuthorization> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", ActionAuthorizationsPage.this);

				int index = cellItem.findParent(Item.class).getIndex();
				
				fragment.add(new AjaxLink<Void>("edit") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ModalPanel(target) {
							
							@Override
							protected Component newContent(String id) {
								return new ActionAuthorizationEditPanel(id, index) {

									@Override
									protected Project getProject() {
										return ActionAuthorizationsPage.this.getProject();
									}

									@Override
									protected void onCancelled(AjaxRequestTarget target) {
										close();
									}

									@Override
									protected void onSaved(AjaxRequestTarget target) {
										target.add(authorizationsTable);
										close();
									}
									
								};
							}
							
						};
					}
					
				});
				
				fragment.add(new AjaxLink<Void>("delete") {

					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						String message = "Do you really want to delete this authorization?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getProject().getBuildSetting().getActionAuthorizations().remove(index);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						Session.get().success("Action authorization deleted");
						target.add(authorizationsTable);
					}

				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<ActionAuthorization, Void> dataProvider = new SortableDataProvider<ActionAuthorization, Void>() {

			@Override
			public Iterator<? extends ActionAuthorization> iterator(long first, long count) {
				return getProject().getBuildSetting().getActionAuthorizations().iterator();
			}

			@Override
			public long size() {
				return getProject().getBuildSetting().getActionAuthorizations().size();
			}

			@Override
			public IModel<ActionAuthorization> model(ActionAuthorization object) {
				return Model.of(object);
			}
		};
		
		add(authorizationsTable = new OneDataTable<>("authorizations", columns, dataProvider, 
				Integer.MAX_VALUE, null));		
		authorizationsTable.setOutputMarkupId(true);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Action Authorizations");
	}

}
