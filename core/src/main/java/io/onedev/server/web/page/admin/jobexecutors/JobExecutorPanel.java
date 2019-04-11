package io.onedev.server.web.page.admin.jobexecutors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.util.ajaxlistener.ConfirmListener;

@SuppressWarnings("serial")
abstract class JobExecutorPanel extends Panel {

	private final JobExecutor executor;
	
	public JobExecutorPanel(String id, JobExecutor executor) {
		super(id);
		
		this.executor = executor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				JobExecutorEditPanel editor = new JobExecutorEditPanel("executor", executor) {

					@Override
					protected void onSave(AjaxRequestTarget target, JobExecutor executor) {
						JobExecutorPanel.this.onSave(target, executor);
						Component executorViewer = BeanContext.viewBean("executor", executor).setOutputMarkupId(true);
						JobExecutorPanel.this.replace(executorViewer);
						target.add(executorViewer);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component executorViewer = BeanContext.viewBean("executor", executor).setOutputMarkupId(true);
						JobExecutorPanel.this.replace(executorViewer);
						target.add(executorViewer);
					}
					
				};
				JobExecutorPanel.this.replace(editor);
				target.add(editor);
			}
			
		});
		
		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmListener("Do you really want to delete this executor?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		AjaxCheckBox enableCheck;
		add(enableCheck = new AjaxCheckBox("enable", Model.of(executor.isEnabled())) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				executor.setEnabled(!executor.isEnabled());
				onSave(target, executor);
				target.add(JobExecutorPanel.this);
			}
			
		});
		add(new WebMarkupContainer("enableLabel") {

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				tag.put("for", enableCheck.getMarkupId(true));
			}
			
		});
		
		add(BeanContext.viewBean("executor", executor).setOutputMarkupId(true));
		
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return !executor.isEnabled()? "disabled": "";
			}
			
		}));
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target, JobExecutor executor);
	
}
