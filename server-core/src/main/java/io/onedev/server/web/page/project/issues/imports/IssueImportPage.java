package io.onedev.server.web.page.project.issues.imports;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.ajaxlistener.ShowGlobalAjaxIndicatorListener;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;

@SuppressWarnings("serial")
public class IssueImportPage<T extends Serializable, S extends Serializable> extends ProjectPage {

	private static final String PARAM_IMPORTER = "importer";
	
	private static final Logger logger = LoggerFactory.getLogger(IssueImportPage.class);
	
	private IssueImporter<T, S> importer;
	
	private T importSource;
	
	private S importOption;
	
	@SuppressWarnings("unchecked")
	public IssueImportPage(PageParameters params) {
		super(params);
		
		String importerName = params.get(PARAM_IMPORTER).toString();
		for (IssueImporterContribution contribution: OneDev.getExtensions(IssueImporterContribution.class)) {
			for (IssueImporter<? extends Serializable, ? extends Serializable> importer: contribution.getImporters()) {
				if (importer.getName().equals(importerName)) {
					this.importer = (IssueImporter<T, S>) importer;
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
		
		try {
			importSource = importer.getSourceClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		Form<?> form = new Form<Void>("form");
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", importSource));
		
		form.add(new AjaxButton("next") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ShowGlobalAjaxIndicatorListener());
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				importOption = importer.getImportOption(importSource, new SimpleLogger() {

					@Override
					public void log(String message) {
						logger.info(message);
					}
					
				});
				
				form.replace(BeanContext.edit("editor", importOption));
				setVisible(false);
				
				form.get("import").setVisible(true);
				form.get("dryRun").setVisible(true);
				
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
		
		Long projectId = getProject().getId();
		
		form.add(new TaskButton("import") {

			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				super.onCompleted(target);
				
				EntitySort sort = new EntitySort();
				sort.setField(Issue.NAME_NUMBER);
				sort.setDirection(Direction.DESCENDING);
				IssueQuery query = new IssueQuery(null, Lists.newArrayList(sort));
				
				PageParameters params = ProjectIssueListPage.paramsOf(getProject(), query.toString(), 0);
				throw new RestartResponseException(ProjectIssueListPage.class, params);
			}

			@Override
			protected String runTask(SimpleLogger logger) {
				return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

					@Override
					public String call() throws Exception {
						Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
						return importer.doImport(project, importSource, importOption, false, logger);
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

		}.setVisible(false));		
		
		form.add(new TaskButton("dryRun") {

			@Override
			protected String runTask(SimpleLogger logger) {
				return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

					@Override
					public String call() throws Exception {
						Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
						return importer.doImport(project, importSource, importOption, true, logger);
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

		}.setVisible(false));		
		
		form.setOutputMarkupId(true);
		add(form);
	}

	public static PageParameters paramsOf(Project project, String importer, String prevUrl) {
		PageParameters params = ProjectPage.paramsOf(project);
		params.add(PARAM_IMPORTER, importer);
		return params;
	}
	
	public T getImportSource() {
		return importSource;
	}
	
	public S getImportOption() {
		return importOption;
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, "Import Issues frmo " + importer.getName());
	}
	
}
