package io.onedev.server.web.page.admin.buildsetting.jobexecutor;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.model.support.administration.jobexecutor.JobExecutor;
import io.onedev.server.util.Path;
import io.onedev.server.util.PathNode;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.component.taskbutton.TaskButton;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.BeanUpdating;
import io.onedev.server.web.editable.EditableUtils;
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
	
	@Nullable
	private JobExecutor getExecutor(String name) {
		for (JobExecutor executor: executors) {
			if (executor.getName().equals(name))
				return executor;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private Class<? extends Serializable> getTestDataClass(Class<?> clazz) {
		Class<? extends Serializable> testDataClass = null;
		for (Type type: clazz.getGenericInterfaces()) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				if (parameterizedType.getRawType() == Testable.class) {
					testDataClass = (Class<? extends Serializable>) parameterizedType.getActualTypeArguments()[0];
					break;
				}
			}
		}
		if (clazz != Object.class && testDataClass == null)
			return getTestDataClass(clazz.getSuperclass());
		else
			return testDataClass;
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
								"This name has already been used by another job executor");
					}
				} else if (getExecutor(executor.getName()) != null) {
					editor.error(new Path(new PathNode.Named("executor"), new PathNode.Named("name")),
							"This name has already been used by another job executor");
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
		
		TaskButton testButton = new TaskButton("testingExecutor") {

			private Serializable testData;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				
				BeanEditor executorEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

					public void component(BeanEditor component, IVisit<BeanEditor> visit) {
						visit.stop(component);
					}
					
				});
				if (executorEditor != null 
						&& executorEditor.isVisibleInHierarchy()
						&& Testable.class.isAssignableFrom(executorEditor.getDescriptor().getBeanClass())) {
					Class<? extends Serializable> testDataClass = getTestDataClass(executorEditor.getDescriptor().getBeanClass());
					if (testDataClass != null) {
						if (testData == null || testData.getClass() != testDataClass) {
							try {
								testData = testDataClass.getDeclaredConstructor().newInstance();
							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
									| InvocationTargetException | NoSuchMethodException | SecurityException e) {
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
				if (editor.isValid()) {
					if (testData != null) {
						String title = EditableUtils.getDisplayName(testData.getClass());
						new BeanEditModalPanel<Serializable>(target, testData, Sets.newHashSet(), true, title) {

							@Override
							protected boolean isDirtyAware() {
								return false;
							}

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
			protected String runTask(TaskLogger logger) {
				((Testable)bean.getExecutor()).test(testData, logger);
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
