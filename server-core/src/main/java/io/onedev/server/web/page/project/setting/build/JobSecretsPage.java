package io.onedev.server.web.page.project.setting.build;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.text.MessageFormat;
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
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.onedev.server.buildspecmodel.inputspec.SecretInput;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.build.JobSecret;
import io.onedev.server.util.CollectionUtils;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.behavior.sortable.SortBehavior;
import io.onedev.server.web.behavior.sortable.SortPosition;
import io.onedev.server.web.component.datatable.DefaultDataTable;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.svg.SpriteImage;

public class JobSecretsPage extends ProjectBuildSettingPage {
	
	private DataTable<JobSecret, Void> secretsTable;
	
	private boolean showArchived;
	
	private Component toggleArchiveButton;
	
	public JobSecretsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		String note = String.format(_T("<svg class='icon mr-2'><use xlink:href='%s'/></svg> " +
						"Define job secrets to be used in build spec. Secrets with <b>same name</b> " +
						"can be defined. For a particular name, the first " +
						"authorized secret with that name will be used (search in current " +
						"project first, then search in parent projects). Note that secret " +
						"value containing line breaks or less than <b>%d</b> characters will " +
						"not be masked in build log"),
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
								target.add(toggleArchiveButton);
								close();
							}
							
						};
					}
					
				};
			}
			
		});
		
		add(toggleArchiveButton = new AjaxLink<Void>("toggleArchived") {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				add(new Label("label", new AbstractReadOnlyModel<String>() {
					@Override
					public String getObject() {
						return showArchived? _T("Hide Archived"): _T("Show Archived");
					}
				}));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				showArchived = !showArchived;
				target.add(this);
				target.add(secretsTable);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getProject().getBuildSetting().getJobSecrets().stream().anyMatch(JobSecret::isArchived));
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
		List<IColumn<JobSecret, Void>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId, IModel<JobSecret> rowModel) {
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

		columns.add(new AbstractColumn<>(Model.of(_T("Name"))) {

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
		
		columns.add(new AbstractColumn<>(Model.of(_T("Authorization"))) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId,
									 IModel<JobSecret> rowModel) {
				if (rowModel.getObject().getAuthorization() != null)
					cellItem.add(new Label(componentId, rowModel.getObject().getAuthorization()));
				else
					cellItem.add(new Label(componentId, "<i>" + _T("Any job") + "</i>").setEscapeModelStrings(false));
			}

		});

		columns.add(new AbstractColumn<>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<JobSecret>> cellItem, String componentId,
									 IModel<JobSecret> rowModel) {
				Fragment fragment = new Fragment(componentId, "actionFrag", JobSecretsPage.this);
				int index = getProject().getBuildSetting().getJobSecrets().indexOf(rowModel.getObject());

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
										target.add(toggleArchiveButton);
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
						String message = MessageFormat.format(_T("Do you really want to delete job secret \"{0}\"?"), rowModel.getObject().getName());
						attributes.getAjaxCallListeners().add(new ConfirmClickListener(message));
					}

					@Override
					public void onClick(AjaxRequestTarget target) {
						var jobSecrets = getProject().getBuildSetting().getJobSecrets();
						var jobSecret = jobSecrets.remove(index);
						var oldAuditContent = VersionedXmlDoc.fromBean(jobSecret).toXML();
						getProjectService().update(getProject());
						auditService.audit(getProject(), "deleted job secret \"" + jobSecret.getName() + "\"", oldAuditContent, null);
						Session.get().success(MessageFormat.format(_T("Job secret \"{0}\" deleted"), rowModel.getObject().getName()));
						target.add(toggleArchiveButton);
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
		
		SortableDataProvider<JobSecret, Void> dataProvider = new SortableDataProvider<>() {

			private List<JobSecret> getDisplaySecrets() {
				return getProject().getBuildSetting().getJobSecrets().stream()
						.filter(it -> showArchived || !it.isArchived())
						.collect(toList());
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
		secretsTable.add(new SortBehavior() {

			@Override
			protected void onSort(AjaxRequestTarget target, SortPosition from, SortPosition to) {
				var secrets = getProject().getBuildSetting().getJobSecrets();
				var oldAuditContent = VersionedXmlDoc.fromBean(secrets).toXML();
				CollectionUtils.move(secrets, from.getItemIndex(), to.getItemIndex());
				var newAuditContent = VersionedXmlDoc.fromBean(secrets).toXML();
				getProjectService().update(getProject());
				auditService.audit(getProject(), "reordered job secrets", oldAuditContent, newAuditContent);
				target.add(secretsTable);
			}

		}.sortable("tbody"));
		
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Job Secrets"));
	}

}
