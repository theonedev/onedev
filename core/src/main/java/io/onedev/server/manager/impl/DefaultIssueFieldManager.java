package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issueworkflow.StateSpec;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.MultiValueIssueField;
import io.onedev.server.util.OneContext;
import io.onedev.server.util.editable.EditableUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.choiceinput.ChoiceInput;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;

@Singleton
public class DefaultIssueFieldManager extends AbstractEntityManager<IssueField> implements IssueFieldManager {

	private static final Logger logger = LoggerFactory.getLogger(DefaultIssueManager.class);
	
	private static final String FIELD_BEAN_PREFIX = "IssueFieldBean";

	private final ProjectManager projectManager;
	
	@Inject
	public DefaultIssueFieldManager(Dao dao, ProjectManager projectManager) {
		super(dao);
		this.projectManager = projectManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Serializable> defineFieldBeanClass(Project project) {
		String className = FIELD_BEAN_PREFIX + project.getId();
		
		return (Class<? extends Serializable>) InputSpec.defineClass(className, project.getIssueWorkflow().getFields());
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
	public Serializable loadFields(Issue issue) {
		BeanDescriptor beanDescriptor = new BeanDescriptor(defineFieldBeanClass(issue.getProject()));
		
		Map<String, PropertyDescriptor> propertyDescriptors = beanDescriptor.getMapOfDisplayNameToPropertyDescriptor();
			
		Serializable fieldBean = (Serializable) beanDescriptor.newBeanInstance();
		
		for (Map.Entry<String, MultiValueIssueField> entry: issue.getMultiValueFields().entrySet()) {
			List<String> values = entry.getValue().getValues();
			Collections.sort(values);
			
			InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getField(entry.getKey());
			if (fieldSpec != null) {
				try {
					propertyDescriptors.get(fieldSpec.getName()).setPropertyValue(
							fieldBean, fieldSpec.convertToObject(values));
				} catch (Exception e) {
					logger.error("Error populating bean for field: " + fieldSpec.getName(), e);
				}
			}
		}
		
		return fieldBean;
	}

	@Transactional
	@Override
	public void saveFields(Issue issue, Serializable fieldBean) {
		Query query = getSession().createQuery("delete from IssueField where issue = :issue");
		query.setParameter("issue", issue);
		query.executeUpdate();
		
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());
		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors()) {
			Object fieldValue = propertyDescriptor.getPropertyValue(fieldBean);
			if (fieldValue != null) {
				InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getField(propertyDescriptor.getDisplayName());
				if (fieldSpec != null) {
					int order;
					
					if (fieldSpec instanceof ChoiceInput) {
						OneContext.push(new OneContext() {

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
							
						});
						
						try {
							List<String> choices = ((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(false);
							order = choices.indexOf(fieldValue);
						} finally {
							OneContext.pop();
						}
					} else {
						order = 0;
					}
					
					for (String eachValue: fieldSpec.convertToStrings(fieldValue)) {
						IssueField field = new IssueField();
						field.setName(fieldSpec.getName());
						field.setIssue(issue);
						field.setType(EditableUtils.getDisplayName(fieldSpec.getClass()));
						field.setValue(eachValue);
						field.setOrder(order);
						save(field);
					}
				}
			}
		}
	}

	@Override
	public Set<String> getExcludedFields(Project project, String state) {
		Map<String, PropertyDescriptor> propertyDescriptors = 
				new BeanDescriptor(defineFieldBeanClass(project)).getMapOfDisplayNameToPropertyDescriptor();
		StateSpec stateSpec = project.getIssueWorkflow().getState(state);
		Set<String> excludedFields = new HashSet<>();
		for (InputSpec fieldSpec: project.getIssueWorkflow().getFields()) {
			if (!stateSpec.getFields().contains(fieldSpec.getName())) 
				excludedFields.add(propertyDescriptors.get(fieldSpec.getName()).getPropertyName());
		}
		return excludedFields;
	}

	@Transactional
	@Override
	public void onRenameGroup(String oldName, String newName) {
		Query query = getSession().createQuery("update IssueField set value=:newName where (type=:groupChoice or type=:groupMultiChoice) and value=:oldName");
		query.setParameter("groupChoice", InputSpec.GROUP_CHOICE);
		query.setParameter("groupMultiChoice", InputSpec.GROUP_MULTI_CHOICE);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void onRenameUser(String oldName, String newName) {
		Query query = getSession().createQuery("update IssueField set value=:newName where (type=:userChoice or type=:userMultiChoice) and value=:oldName");
		query.setParameter("userChoice", InputSpec.USER_CHOICE);
		query.setParameter("userMultiChoice", InputSpec.USER_MULTI_CHOICE);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void renameField(Issue issue, String oldName, String newName) {
		Query query = getSession().createQuery("update IssueField set name=:newName where issue=:issue and name=:oldName");
		query.setParameter("issue", issue);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void renameField(Project project, String oldName, String newName) {
		Query query = getSession().createQuery("update IssueField set name=:newName where "
				+ "name=:oldName and issue.id in (select id from Issue where project=:project)");
		query.setParameter("project", project);
		query.setParameter("oldName", oldName);
		query.setParameter("newName", newName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void deleteField(Issue issue, String fieldName) {
		Query query = getSession().createQuery("delete IssueField where issue=:issue and name=:fieldName");
		query.setParameter("issue", issue);
		query.setParameter("fieldName", fieldName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void deleteField(Project project, String fieldName) {
		Query query = getSession().createQuery("delete IssueField where name=:fieldName and "
				+ "issue.id in (select id from Issue where project=:project)");
		query.setParameter("project", project);
		query.setParameter("fieldName", fieldName);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void renameFieldValue(Issue issue, String fieldName, String oldValue, String newValue) {
		Query query = getSession().createQuery("update IssueField set value=:newValue where issue=:issue and name=:fieldName and value=:oldValue");
		query.setParameter("issue", issue);
		query.setParameter("fieldName", fieldName);
		query.setParameter("oldValue", oldValue);
		query.setParameter("newValue", newValue);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void renameFieldValue(Project project, String fieldName, String oldValue, String newValue) {
		Query query = getSession().createQuery("update IssueField set value=:newValue where "
				+ "name=:fieldName and value=:oldValue and issue.id in (select id from Issue where project=:project)");
		query.setParameter("project", project);
		query.setParameter("fieldName", fieldName);
		query.setParameter("oldValue", oldValue);
		query.setParameter("newValue", newValue);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void deleteFieldValue(Issue issue, String fieldName, String fieldValue) {
		Query query = getSession().createQuery("delete IssueField where issue=:issue and name=:fieldName and value=:fieldValue");
		query.setParameter("issue", issue);
		query.setParameter("fieldName", fieldName);
		query.setParameter("fieldValue", fieldValue);
		query.executeUpdate();
	}

	@Transactional
	@Override
	public void deleteFieldValue(Project project, String fieldName, String fieldValue) {
		Query query = getSession().createQuery("delete IssueField where name=:fieldName and "
				+ "value=:fieldValue and issue.id in (select id from Issue where project=:project)");
		query.setParameter("project", project);
		query.setParameter("fieldName", fieldName);
		query.setParameter("fieldValue", fieldValue);
		query.executeUpdate();
	}
	
}
