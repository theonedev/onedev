package io.onedev.server.web.component.taskbutton;

import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import io.onedev.commons.utils.TaskLogger;
import io.onedev.server.web.component.beaneditmodal.BeanEditModalPanel;
import io.onedev.server.web.editable.BeanEditor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.util.Testable;

public abstract class TestButton extends TaskButton {

	private final BeanEditor editor;

	private final String successMessage;

	private Serializable testData;

	public TestButton(String id, BeanEditor editor, String successMessage) {
		super(id);
		this.editor = editor;
		this.successMessage = successMessage;
	}

	protected abstract Testable<?> getTestable();

	@SuppressWarnings("unchecked")
	@Nullable
	private Class<? extends Serializable> getTestDataClass(Class<?> clazz) {
		Class<? extends Serializable> testDataClass = null;
		for (Type type : clazz.getGenericInterfaces()) {
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
	protected void onConfigure() {
		super.onConfigure();

		BeanEditor childEditor = editor.visitChildren(BeanEditor.class, new IVisitor<BeanEditor, BeanEditor>() {

			public void component(BeanEditor component, IVisit<BeanEditor> visit) {
				visit.stop(component);
			}

		});
		if (childEditor != null
				&& childEditor.isVisibleInHierarchy()
				&& Testable.class.isAssignableFrom(childEditor.getDescriptor().getBeanClass())) {
			Class<? extends Serializable> testDataClass = getTestDataClass(childEditor.getDescriptor().getBeanClass());
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
					protected String onSave(AjaxRequestTarget target, Serializable bean) {
						close();
						target.add(editor);
						target.focusComponent(null);
						submitTask(target);
						return null;
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected TaskResult runTask(TaskLogger logger) {
		((Testable) getTestable()).test(testData, logger);
		return new TaskResult(true, new TaskResult.PlainMessage(_T(successMessage)));
	}

}
