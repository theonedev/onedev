package io.onedev.server.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.exception.OneException;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueConstants;
import io.onedev.server.model.support.issue.workflow.IssueWorkflow;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;

public class IssueUtils {
	
    private static final Set<String> FIX_ISSUE_WORDS = Sets.newHashSet("fix", "fixed", "fixes", "resolve", "resolved", "resolves");
    
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
		setFieldValue(fieldBean, IssueConstants.FIELD_STATE, state);
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
	
	public static Collection<Long> parseFixedIssues(String str) {
		Collection<Long> issueNumbers = new HashSet<>();
		
		StringTokenizer tokenizer = new StringTokenizer(str);
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (FIX_ISSUE_WORDS.contains(token.toLowerCase())) {
				while (FIX_ISSUE_WORDS.contains(parseIssueNumbers(tokenizer, issueNumbers)))
					parseIssueNumbers(tokenizer, issueNumbers);
			}
		}
		
		return issueNumbers;
	}
	
	private static String parseIssueNumbers(StringTokenizer tokenizer, Collection<Long> issueNumbers) {
		boolean issuesFound = false;
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.startsWith("#")) {
				for (String field: Splitter.on(",").omitEmptyStrings().split(token)) {
					if (field.startsWith("#")) {
						field = field.substring(1);
						int index = 0;
						for (char ch: field.toCharArray()) {
							if (Character.isDigit(ch))
								index++;
							else
								break;
						}
						String digits = field.substring(0, index);
						if (digits.length() != 0) {
							issueNumbers.add(Long.parseLong(digits));
							issuesFound = true;
							if (index == field.length())
								continue;
						} 
					} 
					return null;
				}
			} else if (!issuesFound || !token.toLowerCase().equals("and") && !token.equals(",")) {
				return token;
			}
		} 
		return null;
	}
		
}
