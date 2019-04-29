package io.onedev.server.web.page.admin.jobexecutor;

import java.util.List;

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
		
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				JobExecutorEditPanel editor = new JobExecutorEditPanel("executor", executors, executorIndex) {

					@Override
					protected void onSave(AjaxRequestTarget target) {
						JobExecutorPanel.this.onSave(target);
						Component viewer = BeanContext.viewBean("executor", getExecutor()).setOutputMarkupId(true);
						JobExecutorPanel.this.replace(viewer);
						target.add(viewer);
					}

					@Override
					protected void onCancel(AjaxRequestTarget target) {
						Component viewer = BeanContext.viewBean("executor", getExecutor()).setOutputMarkupId(true);
						JobExecutorPanel.this.replace(viewer);
						target.add(viewer);
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
		add(enableCheck = new AjaxCheckBox("enable", Model.of(getExecutor().isEnabled())) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				getExecutor().setEnabled(!getExecutor().isEnabled());
				onSave(target);
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
		
		add(BeanContext.viewBean("executor", getExecutor()).setOutputMarkupId(true));
		
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
	
}
