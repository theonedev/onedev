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
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.model.support.inputspec.SecretInput;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;

@SuppressWarnings("serial")
public class JobSecretsPage extends BuildSettingPage {

	private DataTable<JobSecret, Void> secretsTable;
	
	public JobSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String note = String.format("<svg class='icon mr-2'><use xlink:href='%s'/></svg> "
				+ "Define job secrets to be used in build spec. Secret value less "
				+ "than %d characters will not be masked in build log", 
				SpriteImage.getVersionedHref("bulb"), SecretInput.MASK.length());
		add(new Label("secretsNote", note).setEscapeModelStrings(false));

		add(new AjaxLink<Void>("addNew") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target) {
					
					@Override
					protected String getCssClass() {
						return "modal-lg";
					}
					
					@Override
					protected Component newContent(String id) {
						return new JobSecretEditPanel(id, -1) {

							@Override
							protected Project getProject() {
								return JobSecretsPage.this.getProject();
							}

							@Override
							protected void onCancelled(AjaxRequestTarget target) {
								close();
							}

							@Override
							protected void onSaved(AjaxRequestTarget target) {
								target.add(secretsTable);
								close();
							}
							
						};
					}
					
				};
			}
			
		});
		
		List<IColumn<JobSecret, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<JobSecret, Void>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId, 
					IModel<JobSecret> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().getName()));
			}
			
		});
		
		columns.add(new AbstractColumn<JobSecret, Void>(Model.of("Authorized Branches")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId, 
					IModel<JobSecret> rowModel) {
				if (rowModel.getObject().getAuthorizedBranches() != null)
					cellItem.add(new Label(componentId, rowModel.getObject().getAuthorizedBranches()));
				else
					cellItem.add(new Label(componentId, "<i>All</i>").setEscapeModelStrings(false));
			}
			
		});
		
		columns.add(new AbstractColumn<JobSecret, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId, 
					IModel<JobSecret> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", JobSecretsPage.this);
				int index = cellItem.findParent(Item.class).getIndex();
				
				fragment.add(new AjaxLink<Void>("edit") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						new ModalPanel(target) {
							
							@Override
							protected String getCssClass() {
								return "modal-lg";
							}

							@Override
							protected Component newContent(String id) {
								return new JobSecretEditPanel(id, index) {

									@Override
									protected Project getProject() {
										return JobSecretsPage.this.getProject();
									}

									@Override
									protected void onCancelled(AjaxRequestTarget target) {
										close();
									}

									@Override
									protected void onSaved(AjaxRequestTarget target) {
										target.add(secretsTable);
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
						String message = "Do you really want to delete secret '" + rowModel.getObject().getName() + "'?";
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						getProject().getBuildSetting().getJobSecrets().remove(index);
						OneDev.getInstance(ProjectManager.class).save(getProject());
						Session.get().success("Secret '" + rowModel.getObject().getName() + "' deleted");
						target.add(secretsTable);
					}

				});
				
				cellItem.add(fragment);
			}

			@Override
			public String getCssClass() {
				return "actions";
			}
			
		});
		
		SortableDataProvider<JobSecret, Void> dataProvider = new SortableDataProvider<JobSecret, Void>() {

			@Override
			public Iterator<? extends JobSecret> iterator(long first, long count) {
				return getProject().getBuildSetting().getJobSecrets().iterator();
			}

			@Override
			public long size() {
				return getProject().getBuildSetting().getJobSecrets().size();
			}

			@Override
			public IModel<JobSecret> model(JobSecret object) {
				return Model.of(object);
			}
		};
		
		add(secretsTable = new OneDataTable<>("secrets", columns, dataProvider, 
				Integer.MAX_VALUE, null));		
		secretsTable.setOutputMarkupId(true);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Job Secrets");
	}

}
