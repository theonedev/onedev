package io.onedev.server.web.page.admin.jobexecutor;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import io.onedev.server.model.support.JobExecutor;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.PathElement;
import io.onedev.server.web.util.Testable;

@SuppressWarnings("serial")
abstract class JobExecutorEditPanel extends Panel {

	private final List<JobExecutor> executors;
	
	private final int executorIndex;
	
	public JobExecutorEditPanel(String id, List<JobExecutor> executors, int executorIndex) {
		super(id);
		
		this.executors = executors;
		this.executorIndex = executorIndex;
	}
	
	private void checkNameDuplication(BeanEditor editor, JobExecutor executor) {
		if (executorIndex != -1) { 
			JobExecutor oldExecutor = executors.get(executorIndex);
			if (!executor.getName().equals(oldExecutor.getName()) && getExecutor(executor.getName()) != null) {
				editor.getErrorContext(new PathElement.Named("executor"))
						.getErrorContext(new PathElement.Named("name"))
						.addError("This name has already been used by another job executor");
			}
		} else if (getExecutor(executor.getName()) != null) {
			editor.getErrorContext(new PathElement.Named("executor"))
					.getErrorContext(new PathElement.Named("name"))
					.addError("This name has already been used by another job executor");
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		JobExecutorBean bean = new JobExecutorBean();
		if (executorIndex != -1)
			bean.setExecutor(executors.get(executorIndex));

		BeanEditor editor = BeanContext.edit("editor", bean);
		
		AjaxButton saveButton = new AjaxButton("save") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				JobExecutor executor = bean.getExecutor();
				checkNameDuplication(editor, executor);

				if (!editor.hasErrors(true)) {
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
		
		TaskButton testButton = new TaskButton("testingExecutor") {

			private Serializable testData;

			@SuppressWarnings("unchecked")
			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor jobExecutorEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

					public void component(BeanEditor component, IVisit<BeanEditor> visit) {
						visit.stop(component);
					}
					
				});
				if (jobExecutorEditor != null 
						&& jobExecutorEditor.isVisibleInHierarchy()
						&& Testable.class.isAssignableFrom(jobExecutorEditor.getDescriptor().getBeanClass())) {
					Class<? extends Serializable> testDataClass = null;					
					for (Type type: jobExecutorEditor.getDescriptor().getBeanClass().getGenericInterfaces()) {
						ParameterizedType parameterizedType = (ParameterizedType) type;
						if (parameterizedType.getRawType() == Testable.class) {
							testDataClass = (Class<? extends Serializable>) parameterizedType.getActualTypeArguments()[0];
							break;
						}
					}
					if (testDataClass != null) {
						if (testData == null || testData.getClass() != testDataClass) {
							try {
								testData = testDataClass.newInstance();
							} catch (InstantiationException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						}
					} else {
						testData = null;
					}
					setVisible(true);
				} else {
					setVisible(false);
				}
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				checkNameDuplication(editor, bean.getExecutor());
				if (!editor.hasErrors(true)) {
					if (testData != null) {
						new BeanEditModalPanel(target, testData) {

							@Override
							protected void onSave(AjaxRequestTarget target, Serializable bean) {
								close();
								target.add(editor);
								target.focusComponent(null);
								submitTask(target);
							}
							
						};
					} else {
						target.add(editor);
						target.focusComponent(null);
						submitTask(target);
					}
				} else {
					target.add(form);
				}
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			protected String runTask() {
				((Testable)bean.getExecutor()).test(testData);
				return "Job executor tested successfully";
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

		form.add(new NotificationPanel("feedback", form));
		form.add(editor);
		form.add(saveButton);
		form.add(testButton);
		form.add(cancelButton);
		
		add(form);
		
		setOutputMarkupId(true);
	}
	
	@Nullable
	private JobExecutor getExecutor(String executorName) {
		for (JobExecutor executor: executors) {
			if (executorName.equals(executor.getName()))
				return executor;
		}
		return null;
	}
	
	protected abstract void onSave(AjaxRequestTarget target);
	
	protected abstract void onCancel(AjaxRequestTarget target);
}
