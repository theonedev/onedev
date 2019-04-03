package io.onedev.server.web.page.admin.jobexecutors;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.support.jobexecutor.JobExecutor;
import io.onedev.server.web.editable.PropertyContext;

@SuppressWarnings("serial")
abstract class JobExecutorEditor extends Panel {

	private final JobExecutor executor;
	
	public JobExecutorEditor(String id, JobExecutor executor) {
		super(id);
		
		this.executor = executor;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		Form<?> form = new Form<Void>("form");
		form.add(new NotificationPanel("feedback", form));
		
		JobExecutorBean bean = new JobExecutorBean();
		bean.setJobExecutor(executor);
		form.add(PropertyContext.editBean("editor", bean, "jobExecutor"));
			
		form.add(new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				onSave(target, bean.getJobExecutor());
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		});
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		add(form);
	}

	protected abstract void onSave(AjaxRequestTarget target, JobExecutor executor);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
