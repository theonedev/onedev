package io.onedev.server.web.page.admin.buildsetting.jobexecutor;

import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.component.taskbutton.TestButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.util.Testable;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import org.jspecify.annotations.Nullable;

import static io.onedev.server.web.translation.Translation._T;

import java.util.List;

abstract class JobExecutorEditPanel extends Panel {

	private final List<JobExecutor> executors;
	
	private final int executorIndex;
	
	public JobExecutorEditPanel(String id, List<JobExecutor> executors, int executorIndex) {
		super(id);
		
		this.executors = executors;
		this.executorIndex = executorIndex;
	}
	
	@Nullable
	private JobExecutor getExecutor(String name) {
		for (JobExecutor executor: executors) {
			if (executor.getName().equals(name))
				return executor;
		}
		return null;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobExecutorBean bean = new JobExecutorBean();
		if (executorIndex != -1)
			bean.setExecutor(SerializationUtils.clone(executors.get(executorIndex)));

		BeanEditor editor = BeanContext.edit("editor", bean);
		
		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				JobExecutor executor = bean.getExecutor();
				if (executorIndex != -1) { 
					JobExecutor oldExecutor = executors.get(executorIndex);
					if (!executor.getName().equals(oldExecutor.getName()) && getExecutor(executor.getName()) != null) {
						editor.error(new Path(new PathNode.Named("executor"), new PathNode.Named("name")),
								_T("This name has already been used by another job executor"));
					}
				} else if (getExecutor(executor.getName()) != null) {
					editor.error(new Path(new PathNode.Named("executor"), new PathNode.Named("name")),
							_T("This name has already been used by another job executor"));
				}
				
				if (editor.isValid()) {
					if (executorIndex != -1) {
						executors.set(executorIndex, executor);
					} else {
						executors.add(executor);
					}
					onSave(target);
				} else {
					target.add(form);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}
			
		};
		AjaxLink<Void> cancelButton = new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		};
		
		TestButton testButton = new TestButton("testingExecutor", editor, "Job executor tested successfully") {

			@Override
			protected Testable<?> getTestable() {
				return (Testable<?>) bean.getExecutor();
			}

		};		
		
		Form<?> form = new Form<Void>("form") {
			
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);

				if (event.getPayload() instanceof BeanUpdating) {
					BeanUpdating beanUpdating = (BeanUpdating) event.getPayload();
					beanUpdating.getHandler().add(testButton);
				}
				
			}
			
		};

		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(editor);
		form.add(saveButton);
		form.add(testButton);
		form.add(cancelButton);
		
		add(form);
		
		setOutputMarkupId(true);
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
