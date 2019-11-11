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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.model.support.issue.fieldspec.FieldSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;

public class IssueUtils {
	
    private static final Set<String> FIX_ISSUE_WORDS = Sets.newHashSet("fix", "fixed", "fixes", "resolve", "resolved", "resolves");
    
	private static final String FIELD_BEAN_PREFIX = "IssueFieldBean";
	
	public static void clearFields(Serializable fieldBean) {
		for (List<PropertyDescriptor> groupProperties: new BeanDescriptor(fieldBean.getClass()).getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) 
				property.setPropertyValue(fieldBean, null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends Serializable> defineFieldBeanClass(Project project) {
		String className = FIELD_BEAN_PREFIX + project.getId();
		IssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		return (Class<? extends Serializable>) FieldSpec.defineClass(className, "Issue Fields", issueSetting.getFieldSpecs());
	}
	
	@Nullable
	public static Class<? extends Serializable> loadFieldBeanClass(String className) {
		if (className.startsWith(FIELD_BEAN_PREFIX)) {
			Long projectId = Long.parseLong(className.substring(FIELD_BEAN_PREFIX.length()));
			Project project = OneDev.getInstance(ProjectManager.class).load(projectId);
			return defineFieldBeanClass(project);
		} else {
			return null;
		}
	}
	
	public static Collection<String> getPropertyNames(Project project, Class<?> fieldBeanClass, Collection<String> fieldNames) {
		Collection<String> propertyNames = new HashSet<>();
		SettingManager settingManager = OneDev.getInstance(SettingManager.class); 
		for (List<PropertyDescriptor> groupProperties: new BeanDescriptor(fieldBeanClass).getProperties().values()) {
			for (PropertyDescriptor property: groupProperties) {
				if (fieldNames.contains(property.getDisplayName())) {
					FieldSpec field = settingManager.getIssueSetting().getFieldSpec(property.getDisplayName());
					if (field != null && SecurityUtils.canEditIssueField(project, field.getName()))
						propertyNames.add(property.getPropertyName());
				}
			}
		}
		return propertyNames;
	}
	
	public static Map<String, Object> getFieldValues(ComponentContext context, Serializable fieldBean, Collection<String> fieldNames) {
		ComponentContext.push(context);
		try {
			Map<String, Object> fieldValues = new HashMap<>();
			BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());
			for (List<PropertyDescriptor> groupProperties: beanDescriptor.getProperties().values()) {
				for (PropertyDescriptor property: groupProperties) {
					if (fieldNames.contains(property.getDisplayName()))
						fieldValues.put(property.getDisplayName(), property.getPropertyValue(fieldBean));
				}
			}
			
			return fieldValues;
		} finally {
			ComponentContext.pop();
		}
	}
	
	public static Collection<Long> parseFixedIssues(Project project, String commitMessage) {
		Collection<Long> issueNumbers = new HashSet<>();

		// Skip unmatched commit message quickly 
		boolean found = false;
		String lowerCaseCommitMessage = commitMessage.toLowerCase();
		for (String word: FIX_ISSUE_WORDS) {
			if (lowerCaseCommitMessage.indexOf(word) != -1) {
				found = true;
				break;
			}
		}
		
		if (found) {
			/*
			 * Transform commit message with defined transformers first in order not to process issue keys pointing 
			 * to external issue trackers
			 */
			commitMessage = CommitMessageTransformer.transform(commitMessage, project.getCommitMessageTransforms());
			
			// Only process top level text node to skip transformed links above
			Element body = Jsoup.parseBodyFragment(commitMessage).body();
			for (int i=0; i<body.childNodeSize(); i++) {
				Node node = body.childNode(i);
				if (node instanceof TextNode) {
					TextNode textNode = (TextNode) body.childNode(i);
					StringTokenizer tokenizer = new StringTokenizer(textNode.getWholeText());
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						if (FIX_ISSUE_WORDS.contains(token.toLowerCase())) {
							while (FIX_ISSUE_WORDS.contains(parseIssueNumbers(tokenizer, issueNumbers)))
								parseIssueNumbers(tokenizer, issueNumbers);
						}
					}
				}
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
