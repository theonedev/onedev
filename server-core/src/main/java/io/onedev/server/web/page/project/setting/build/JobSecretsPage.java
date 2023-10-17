package io.onedev.server.web.page.project.setting.build;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.*;

@SuppressWarnings("serial")
public class JobSecretsPage extends ProjectBuildSettingPage {

	private DataTable<JobSecret, Void> secretsTable;
	
	private Component showArchivedButton;
	
	public JobSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String note = String.format("<svg class='icon mr-2'><use xlink:href='%s'/></svg> "
				+ "Define job secrets to be used in build spec. Secrets defined in parent "
				+ "project will also be availalbe for use. If secret of same name is defined "
				+ "in both parent and child project, the one defined in child project will "
				+ "take effect. Note that Secret value less "
				+ "than %d characters will not be masked in build log", 
				SpriteImage.getVersionedHref("bulb"), SecretInput.MASK.length());
		add(new Label("secretsNote", note).setEscapeModelStrings(false));

		add(new AjaxLink<Void>("addNew") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				new ModalPanel(target) {
					
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
								target.add(showArchivedButton);
								close();
							}
							
						};
					}
					
				};
			}
			
		});
		
		add(showArchivedButton = new AjaxLink<Void>("showArchived") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				setVisibilityAllowed(false);
				target.add(showArchivedButton);
				target.add(secretsTable);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getBuildSetting().getJobSecrets().stream().anyMatch(JobSecret::isArchived));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		List<IColumn<JobSecret, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<>(Model.of("Name")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId,
									 IModel<JobSecret> rowModel) {
				var secret = rowModel.getObject();
				var fragment = new Fragment(componentId, "nameFrag", JobSecretsPage.this);
				fragment.add(new Label("name", rowModel.getObject().getName()));
				fragment.add(new WebMarkupContainer("archived").setVisible(secret.isArchived()));
				cellItem.add(fragment);
			}

		});
		
		columns.add(new AbstractColumn<>(Model.of("Authorization")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId,
									 IModel<JobSecret> rowModel) {
				if (rowModel.getObject().getAuthorization() != null)
					cellItem.add(new Label(componentId, rowModel.getObject().getAuthorization()));
				else
					cellItem.add(new Label(componentId, "<i>All Branches</i>").setEscapeModelStrings(false));
			}

		});

		columns.add(new AbstractColumn<>(Model.of("")) {

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
										target.add(showArchivedButton);
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
						OneDev.getInstance(ProjectManager.class).update(getProject());
						Session.get().success("Secret '" + rowModel.getObject().getName() + "' deleted");
						target.add(showArchivedButton);
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

			private List<JobSecret> getDisplaySecrets() {
				return getProject().getBuildSetting().getJobSecrets().stream()
						.filter(it-> !showArchivedButton.isVisibilityAllowed() || !it.isArchived())
						.sorted((o1, o2) -> {
							if (o1.isArchived() && !o2.isArchived())
								return 1;
							else if (!o1.isArchived() && o2.isArchived())
								return -1;
							else
								return 0;
						}).collect(toList());
			}
			
			@Override
			public Iterator<? extends JobSecret> iterator(long first, long count) {
				return getDisplaySecrets().iterator();
			}

			@Override
			public long size() {
				return getDisplaySecrets().size();
			}

			@Override
			public IModel<JobSecret> model(JobSecret object) {
				return Model.of(object);
			}
		};
		
		add(secretsTable = new DefaultDataTable<>("secrets", columns, dataProvider, Integer.MAX_VALUE, null));		
		secretsTable.setOutputMarkupId(true);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Job Secrets");
	}

}
