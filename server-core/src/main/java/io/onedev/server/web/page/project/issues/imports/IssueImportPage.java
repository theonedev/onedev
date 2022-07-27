package io.onedev.server.web.page.project.issues.imports;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.Lists;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.IssueManager;
import io.onedev.server.imports.IssueImporter;
import io.onedev.server.imports.IssueImporterContribution;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.component.wizard.WizardPanel;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.dashboard.ProjectDashboardPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;

@SuppressWarnings("serial")
public class IssueImportPage<Where extends Serializable, What extends Serializable, How extends Serializable> extends ProjectPage {

	private static final String PARAM_IMPORTER = "importer";
	
	private IssueImporter importer;
	
	public IssueImportPage(PageParameters params) {
		super(params);
		
		String importerName = params.get(PARAM_IMPORTER).toString();
		for (IssueImporterContribution contribution: OneDev.getExtensions(IssueImporterContribution.class)) {
			for (IssueImporter importer: contribution.getImporters()) {
				if (importer.getName().equals(importerName)) {
					this.importer = importer;
					break;
				}
			}
		}
		
		if (importer == null)
			throw new RuntimeException("Undefined importer: " + importerName);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WizardPanel("wizard", importer.getSteps()) {

			@Override
			protected WebMarkupContainer newEndActions(String componentId) {
				Fragment fragment = new Fragment(componentId, "endActionsFrag", IssueImportPage.this);
				
				fragment.add(new TaskButton("import") {

					@Override
					protected void onCompleted(AjaxRequestTarget target, boolean successful) {
						super.onCompleted(target, successful);

						if (successful) {
							EntitySort sort = new EntitySort();
							sort.setField(Issue.NAME_NUMBER);
							sort.setDirection(Direction.DESCENDING);
							IssueQuery query = new IssueQuery(null, Lists.newArrayList(sort));
							
							PageParameters params = ProjectIssueListPage.paramsOf(getProject(), query.toString(), 0);
							throw new RestartResponseException(ProjectIssueListPage.class, params);
						}
					}

					@Override
					protected String runTask(TaskLogger logger) {
						return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

							@Override
							public String call() throws Exception {
								return doImport(false, logger);
							}
							
						});
					}
					
					@Override
					protected String getTitle() {
						return "Importing from " + importer.getName();
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}

				});		
				
				fragment.add(new TaskButton("dryRun") {

					@Override
					protected String runTask(TaskLogger logger) {
						return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

							@Override
							public String call() throws Exception {
								return doImport(true, logger);
							}
							
						});
					}
					
					@Override
					protected String getTitle() {
						return "Test importing from " + importer.getName();
					}

					@Override
					protected void onError(AjaxRequestTarget target, Form<?> form) {
						super.onError(target, form);
						target.add(form);
					}

				});		
				
				return fragment;
			}
			
		});
		
	}
	
	private String doImport(boolean dryRun, TaskLogger logger) {
		IssueManager issueManager = OneDev.getInstance(IssueManager.class);
		Project numberScope = getProject().getForkRoot();
		try {
			issueManager.resetNextNumber(numberScope);
			Long nextNumber = issueManager.getNextNumber(numberScope);
			issueManager.resetNextNumber(numberScope);
			return importer.doImport(getProject(), nextNumber==1L, dryRun, logger);
		} finally {
			issueManager.resetNextNumber(numberScope);
		}
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getUser() != null;
	}
	
	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Importing Issues from " + importer.getName());
	}
	
	public static PageParameters paramsOf(Project project, String importer) {
		PageParameters params = ProjectPage.paramsOf(project);
		params.add(PARAM_IMPORTER, importer);
		return params;
	}
	
	@Override
	protected void navToProject(Project project) {
		if (project.isIssueManagement()) 
			setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(project, 0));
		else
			setResponsePage(ProjectDashboardPage.class, ProjectDashboardPage.paramsOf(project.getId()));
	}
	
}
