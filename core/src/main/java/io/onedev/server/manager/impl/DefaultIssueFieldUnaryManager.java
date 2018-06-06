package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.exception.OneException;
import io.onedev.server.manager.IssueFieldUnaryManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldUnary;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.IssueField;
import io.onedev.server.model.support.issue.workflow.StateSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.EditableUtils;
import io.onedev.server.web.editable.PropertyDescriptor;

@Singleton
public class DefaultIssueFieldUnaryManager extends AbstractEntityManager<IssueFieldUnary> implements IssueFieldUnaryManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueManager.class);
	
	private static final String FIELD_BEAN_PREFIX = "IssueFieldBean";

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultIssueFieldUnaryManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Serializable> defineFieldBeanClass(Project project) {
		String className = FIELD_BEAN_PREFIX + project.getId();
		
		return (Class<? extends Serializable>) InputSpec.defineClass(className, project.getIssueWorkflow().getFieldSpecs());
	}
	
	@Override
	public Class<? extends Serializable> loadFieldBeanClass(String className) {
		if (className.startsWith(FIELD_BEAN_PREFIX)) {
			Long projectId = Long.valueOf(className.substring(FIELD_BEAN_PREFIX.length()));
			return defineFieldBeanClass(projectManager.load(projectId));
		} else {
			return null;
		}
	}

	@Sessional
	@Override
	public Serializable readFields(Issue issue) {
		BeanDescriptor beanDescriptor = new BeanDescriptor(defineFieldBeanClass(issue.getProject()));
		
		Map<String, PropertyDescriptor> propertyDescriptors = beanDescriptor.getMapOfDisplayNameToPropertyDescriptor();
			
		Serializable fieldBean = (Serializable) beanDescriptor.newBeanInstance();

		for (Map.Entry<String, IssueField> entry: issue.getEffectiveFields().entrySet()) {
			List<String> strings = entry.getValue().getValues();
			Collections.sort(strings);
			
			InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getFieldSpec(entry.getKey());
			if (fieldSpec != null) {
				try {
					Object fieldValue;
					if (!strings.isEmpty())
						fieldValue = fieldSpec.convertToObject(strings);
					else
						fieldValue = null;
					propertyDescriptors.get(fieldSpec.getName()).setPropertyValue(fieldBean, fieldValue);
				} catch (Exception e) {
					logger.error("Error populating bean for field: " + fieldSpec.getName(), e);
				}
			}
		}
		
		return fieldBean;
	}

	@Transactional
	@Override
	public void writeFields(Issue issue, Serializable fieldBean, Collection<String> fieldNames) {
		if (!fieldNames.isEmpty()) {
			Query query = getSession().createQuery("delete from IssueFieldUnary where issue = :issue and name in (:names)");
			query.setParameter("issue", issue);
			query.setParameter("names", fieldNames);
			query.executeUpdate();
			for (Iterator<IssueFieldUnary> it = issue.getFieldUnaries().iterator(); it.hasNext();) {
				if (fieldNames.contains(it.next().getName()))
					it.remove();
			}
		}
		
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());

		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors()) {
			String fieldName = propertyDescriptor.getDisplayName();
			if (fieldNames.contains(fieldName)) {
				Object fieldValue = propertyDescriptor.getPropertyValue(fieldBean);
				InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getFieldSpec(fieldName);
				if (fieldSpec != null) {
					long ordinal = fieldSpec.getOrdinal(new OneContext() {

						@Override
						public Project getProject() {
							return issue.getProject();
						}

						@Override
						public EditContext getEditContext(int level) {
							return new EditContext() {

								@Override
								public Object getInputValue(String name) {
									return beanDescriptor.getMapOfDisplayNameToPropertyDescriptor().get(name).getPropertyValue(fieldBean);
								}
								
							};
						}

						@Override
						public InputContext getInputContext() {
							throw new UnsupportedOperationException();
						}
						
					}, fieldValue);

					IssueFieldUnary field = new IssueFieldUnary();
					field.setIssue(issue);
					field.setName(fieldName);
					field.setOrdinal(ordinal);
					field.setType(EditableUtils.getDisplayName(fieldSpec.getClass()));
					
					if (fieldValue != null) {
						List<String> strings = fieldSpec.convertToStrings(fieldValue);
						if (!strings.isEmpty()) {
							for (String string: strings) {
								IssueFieldUnary cloned = (IssueFieldUnary) SerializationUtils.clone(field);
								cloned.setIssue(issue);
								cloned.setValue(string);
								save(cloned);
								issue.getFieldUnaries().add(cloned);
							}
						} else {
							save(field);
							issue.getFieldUnaries().add(field);
						}
					} else {
						save(field);
						issue.getFieldUnaries().add(field);
					}
				}
			}
		}
	}

	@Override
	public Set<String> getExcludedProperties(Issue issue, String state) {
		Map<String, PropertyDescriptor> propertyDescriptors = 
				new BeanDescriptor(defineFieldBeanClass(issue.getProject())).getMapOfDisplayNameToPropertyDescriptor();
		StateSpec stateSpec = issue.getProject().getIssueWorkflow().getStateSpec(state);
		if (stateSpec == null)
			throw new OneException("Unable to find state spec: " + state);
		Set<String> excludedProperties = new HashSet<>();
		for (InputSpec fieldSpec: issue.getProject().getIssueWorkflow().getFieldSpecs()) {
			if (!stateSpec.getFields().contains(fieldSpec.getName()) 
					|| issue.getEffectiveFields().containsKey(fieldSpec.getName())) { 
				excludedProperties.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
			}
		}
		return excludedProperties;
	}

	@Transactional
	@Override
	public void onRenameGroup(String oldName, String newName) {
		Query query = getSession().createQuery("update IssueFieldUnary set value=:newName where type=:groupChoice and value=:oldName");
		query.setParameter("groupChoice", InputSpec.GROUP_CHOICE);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onRenameUser(String oldName, String newName) {
		Query query = getSession().createQuery("update IssueFieldUnary set value=:newName where type=:userChoice and value=:oldName");
		query.setParameter("userChoice", InputSpec.USER_CHOICE);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Sessional
	@Override
	public void populateFields(List<Issue> issues) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueFieldUnary> query = builder.createQuery(IssueFieldUnary.class);
		
		Root<IssueFieldUnary> root = query.from(IssueFieldUnary.class);
		query.select(root);
		root.join("issue");
		
		Expression<String> issueExpr = root.get("issue");
		query.where(issueExpr.in(issues));
		
		for (Issue issue: issues)
			issue.setFieldUnaries(new ArrayList<>());
		
		for (IssueFieldUnary field: getSession().createQuery(query).getResultList())
			field.getIssue().getFieldUnaries().add(field);
	}
	
}
