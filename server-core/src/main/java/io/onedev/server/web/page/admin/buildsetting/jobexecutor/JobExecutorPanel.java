package io.onedev.server.web.page.admin.buildsetting.jobexecutor;

import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.web.ajaxlistener.ConfirmClickListener;
import io.onedev.server.web.editable.BeanContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

abstract class JobExecutorPanel extends Panel {

	private final List<JobExecutor> executors;
	
	private final int executorIndex;
	
	public JobExecutorPanel(String id, List<JobExecutor> executors, int executorIndex) {
		super(id);
		
		this.executors = executors;
		this.executorIndex = executorIndex;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new Label("name", getExecutor().getName()));
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				JobExecutorEditPanel editor = new JobExecutorEditPanel("executor", executors, executorIndex) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						JobExecutorPanel.this.onSave(target);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						JobExecutorPanel.this.onCancel(target);
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
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this executor?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		add(new WebMarkupContainer("disabled") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getExecutor().isEnabled());
			}
			
		});
		
		add(new AjaxCheckBox("enable", Model.of(getExecutor().isEnabled())) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				getExecutor().setEnabled(!getExecutor().isEnabled());
				onSave(target);
				target.add(JobExecutorPanel.this);
			}
			
		});
		
		var bean = new JobExecutorBean();
		bean.setExecutor(getExecutor());
		add(BeanContext.view("executor", bean).setOutputMarkupId(true));
		
		add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return !getExecutor().isEnabled()? "disabled": "";
			}
			
		}));
		
		setOutputMarkupId(true);
	}
	
	private JobExecutor getExecutor() {
		return executors.get(executorIndex);
	}
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
