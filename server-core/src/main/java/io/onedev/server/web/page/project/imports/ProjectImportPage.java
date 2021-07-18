package io.onedev.server.web.page.project.imports;

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
import io.onedev.server.buildspec.job.log.StyleBuilder;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.search.entity.EntitySort;
import io.onedev.server.search.entity.EntitySort.Direction;
import io.onedev.server.search.entity.project.ProjectQuery;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.SimpleLogger;
import io.onedev.server.web.ajaxlistener.ShowGlobalAjaxIndicatorListener;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.layout.LayoutPage;
import io.onedev.server.web.page.project.ProjectListPage;

@SuppressWarnings("serial")
public class ProjectImportPage<T extends Serializable, S extends Serializable> extends LayoutPage {

	private static final String PARAM_IMPORTER = "importer";
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectImportPage.class);
	
	private ProjectImporter<T, S> importer;
	
	private T importSource;
	
	private S importOption;
	
	@SuppressWarnings("unchecked")
	public ProjectImportPage(PageParameters params) {
		super(params);
		
		String importerName = params.get(PARAM_IMPORTER).toString();
		for (ProjectImporterContribution contribution: OneDev.getExtensions(ProjectImporterContribution.class)) {
			for (ProjectImporter<? extends Serializable, ? extends Serializable> importer: contribution.getImporters()) {
				if (importer.getName().equals(importerName)) {
					this.importer = (ProjectImporter<T, S>) importer;
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
					public void log(String message, StyleBuilder styleBuilder) {
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
		
		form.add(new TaskButton("import") {

			@Override
			protected void onCompleted(AjaxRequestTarget target, boolean successful) {
				super.onCompleted(target, successful);

				if (successful) {
					EntitySort sort = new EntitySort();
					sort.setField(Project.NAME_UPDATE_DATE);
					sort.setDirection(Direction.DESCENDING);
					ProjectQuery query = new ProjectQuery(null, Lists.newArrayList(sort));
					PageParameters params = ProjectListPage.paramsOf(query.toString(), 0, 0);
					throw new RestartResponseException(ProjectListPage.class, params); 
				}
			}

			@Override
			protected String runTask(SimpleLogger logger) {
				return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

					@Override
					public String call() throws Exception {
						return importer.doImport(importSource, importOption, false, logger);
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
						return importer.doImport(importSource, importOption, true, logger);
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

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.getUser() != null;
	}
	
	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, "Importing Projects from " + importer.getName());
	}
	
	public static PageParameters paramsOf(String importer) {
		PageParameters params = new PageParameters();
		params.add(PARAM_IMPORTER, importer);
		return params;
	}
	
	public T getImportSource() {
		return importSource;
	}
	
	public S getImportOption() {
		return importOption;
	}
	
}
