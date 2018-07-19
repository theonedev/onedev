package io.onedev.server.web.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;

public class IssueFieldBeanUtils {
	
	private static final String BEAN_PREFIX = "IssueFieldBean";
	
	public static void clearFields(Serializable fieldBean) {
		for (PropertyDescriptor property: new BeanDescriptor(fieldBean.getClass()).getPropertyDescriptors())
			property.setPropertyValue(fieldBean, null);
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineBeanClass(Project project) {
		String className = BEAN_PREFIX + project.getId();
		IssueWorkflow workflow = project.getIssueWorkflow();
		List<InputSpec> fieldSpecs = Lists.newArrayList(workflow.getFieldSpecOfState());
		fieldSpecs.addAll(workflow.getFieldSpecs());
		return (Class<? extends Serializable>) InputSpec.defineClass(className, fieldSpecs);
	}
	
	@Nullable
	public static Class<? extends Serializable> loadBeanClass(String className) {
		if (className.startsWith(BEAN_PREFIX)) {
			Long projectId = Long.parseLong(className.substring(BEAN_PREFIX.length()));
			Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
			return defineBeanClass(project);
		} else {
			return null;
		}
	}
	
	public static void setState(Serializable fieldBean, String state) {
		setFieldValue(fieldBean, Issue.FIELD_STATE, state);
	}
	
	@Nullable
	public static Object getFieldValue(Serializable fieldBean, String fieldName) {
		for (PropertyDescriptor property: new BeanDescriptor(fieldBean.getClass()).getPropertyDescriptors()) {
			if (fieldName.equals(property.getDisplayName()))
				return property.getPropertyValue(fieldBean);
		}
		throw new OneException("Unable to find property for field: " + fieldName);
	}
	
	public static void setFieldValue(Serializable fieldBean, String fieldName, Object fieldValue) {
		for (PropertyDescriptor property: new BeanDescriptor(fieldBean.getClass()).getPropertyDescriptors()) {
			if (property.getDisplayName().equals(fieldName))
				property.setPropertyValue(fieldBean, fieldValue);
		}
	}
	
	public static Collection<String> getPropertyNames(Class<?> fieldBeanClass, Collection<String> fieldNames) {
		Collection<String> propertyNames = new HashSet<>();
		for (PropertyDescriptor property: new BeanDescriptor(fieldBeanClass).getPropertyDescriptors()) {
			if (fieldNames.contains(property.getDisplayName()))
				propertyNames.add(property.getPropertyName());
		}
		return propertyNames;
	}
	
	public static Map<String, Object> getFieldValues(Serializable fieldBean) {
		Map<String, Object> fieldValues = new HashMap<>();
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());
		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors())
			fieldValues.put(propertyDescriptor.getDisplayName(), propertyDescriptor.getPropertyValue(fieldBean));
		
		return fieldValues;
	}
	
}
