package io.onedev.server.web.component.imports;

import java.io.Serializable;
import java.util.concurrent.Callable;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.OneDev;
import io.onedev.server.imports.Importer;
import io.onedev.server.persistence.TransactionManager;
import io.onedev.server.web.ajaxlistener.ShowGlobalAjaxIndicatorListener;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class ImportPanel<Where extends Serializable, What extends Serializable, How extends Serializable> 
		extends Panel {

	private static final Logger logger = LoggerFactory.getLogger(ImportPanel.class);
	
	private Where where;
	
	private What what;
	
	private How how;
	
	public ImportPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		try {
			where = getImporter().getWhereClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		Form<?> form = new Form<Void>("form");
		form.add(new WebMarkupContainer("retainNumbersNote")
				.setVisible(getPage() instanceof ProjectPage));
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", where));
		
		form.add(new AjaxLink<Void>("back") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if (how != null) {
					how = null;
					form.replace(BeanContext.edit("editor", what));
					form.get("import").setVisible(false);
					form.get("dryRun").setVisible(false);
				} else {
					what = null;
					form.replace(BeanContext.edit("editor", where));
					setVisible(false);
				}

				form.get("next").setVisible(true);
				
				target.add(form);
			}

		}.setVisible(false));
		
		form.add(new AjaxButton("next") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ShowGlobalAjaxIndicatorListener());
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				if (what == null) {
					what = getImporter().getWhat(where, new TaskLogger() {

						@Override
						public void log(String message, String sessionId) {
							logger.info(message);
						}
						
					});
					form.replace(BeanContext.edit("editor", what));
					form.get("back").setVisible(true);
				} else {
					how = getImporter().getHow(where, what, new TaskLogger() {

						@Override
						public void log(String message, String sessionId) {
							logger.info(message);
						}
						
					});
					form.replace(BeanContext.edit("editor", how));
					form.get("next").setVisible(false);
					form.get("import").setVisible(true);
					form.get("dryRun").setVisible(true);
				}
				
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

				if (successful)
					onImportSuccessful(target);
			}

			@Override
			protected String runTask(TaskLogger logger) {
				return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

					@Override
					public String call() throws Exception {
						return doImport(where, what, how, false, logger);
					}
					
				});
			}
			
			@Override
			protected String getTitle() {
				return "Importing from " + getImporter().getName();
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		}.setVisible(false));		
		
		form.add(new TaskButton("dryRun") {

			@Override
			protected String runTask(TaskLogger logger) {
				return OneDev.getInstance(TransactionManager.class).call(new Callable<String>() {

					@Override
					public String call() throws Exception {
						return doImport(where, what, how, true, logger);
					}
					
				});
			}
			
			@Override
			protected String getTitle() {
				return "Test importing from " + getImporter().getName();
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

	protected abstract Importer<Where, What, How> getImporter();
	
	@Nullable 
	protected abstract String doImport(Where where, What what, How how, 
			boolean dryRun, TaskLogger logger);
	
	protected abstract void onImportSuccessful(AjaxRequestTarget target);
	
}
