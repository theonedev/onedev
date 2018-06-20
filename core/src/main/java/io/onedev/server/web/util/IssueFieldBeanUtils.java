package io.onedev.server.web.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.onedev.server.OneDev;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.utils.StringUtils;

public class IssueFieldBeanUtils {
	
	private static final String BEAN_PREFIX = "IssueFieldBean";
	
	public static Map<String, Object> getFieldValues(Serializable fieldBean, Collection<String> fieldNames) {
		Map<String, Object> fieldValues = new HashMap<>();
		for (PropertyDescriptor property: new BeanDescriptor(fieldBean.getClass()).getPropertyDescriptors()) {
			if (fieldNames.contains(property.getDisplayName()))
				fieldValues.put(property.getDisplayName(), property.getPropertyValue(fieldBean));
		}
		return fieldValues;
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineBeanClass(Project project, boolean setDefaultValue) {
		String className = BEAN_PREFIX + "_" + setDefaultValue + "_" + project.getId();
		return (Class<? extends Serializable>) InputSpec.defineClass(className, 
				project.getIssueWorkflow().getFieldSpecs(), setDefaultValue);
	}
	
	@Nullable
	public static Class<? extends Serializable> loadBeanClass(String className) {
		if (className.startsWith(BEAN_PREFIX)) {
			String tempStr = className.substring(BEAN_PREFIX.length()+1);
			boolean setDefaultValue = Boolean.parseBoolean(StringUtils.substringBefore(tempStr, "_"));
			Long projectId = Long.parseLong(StringUtils.substringAfter(tempStr, "_"));
			Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
			return defineBeanClass(project, setDefaultValue);
		} else {
			return null;
		}
	}
	
}
