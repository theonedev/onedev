package io.onedev.server.manager.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.onedev.server.manager.IssueFieldManager;
import io.onedev.server.manager.ProjectManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.Project;
import io.onedev.server.model.support.issue.workflow.StateSpec;
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
import io.onedev.server.util.inputspec.dateinput.DateInput;
import io.onedev.server.util.inputspec.multichoiceinput.MultiChoiceInput;
import io.onedev.server.util.inputspec.numberinput.NumberInput;
import io.onedev.server.web.editable.BeanDescriptor;
import io.onedev.server.web.editable.PropertyDescriptor;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.InvalidFieldResolution;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.page.project.issues.issuelist.workflowreconcile.UndefinedFieldValueResolution;

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
	public Serializable readFields(Issue issue) {
		BeanDescriptor beanDescriptor = new BeanDescriptor(defineFieldBeanClass(issue.getProject()));
		
		Map<String, PropertyDescriptor> propertyDescriptors = beanDescriptor.getMapOfDisplayNameToPropertyDescriptor();
			
		Serializable fieldBean = (Serializable) beanDescriptor.newBeanInstance();
		
		for (Map.Entry<String, MultiValueIssueField> entry: issue.getMultiValueFields().entrySet()) {
			List<String> strings = entry.getValue().getValues();
			Collections.sort(strings);
			
			InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getField(entry.getKey());
			if (fieldSpec != null) {
				try {
					Object fieldValue;
					if (!strings.isEmpty())
						fieldValue = fieldSpec.convertToObject(strings);
					else
						fieldValue = null;
					propertyDescriptors.get(fieldSpec.getName()).setPropertyValue(
							fieldBean, fieldValue);
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
			CriteriaBuilder builder = getSession().getCriteriaBuilder();
			CriteriaDelete<IssueField> criteriaDelete = builder.createCriteriaDelete(IssueField.class);
			Root<IssueField> root = criteriaDelete.from(IssueField.class);
			Predicate issuePredicate = builder.equal(root.get("issue"), issue);
			Predicate namePredicate = root.get("name").in(fieldNames);
			criteriaDelete.where(builder.and(issuePredicate, namePredicate));
			
			getSession().createQuery(criteriaDelete).executeUpdate();
			
			Query query = getSession().createQuery("delete from IssueField where issue = :issue");
			query.setParameter("issue", issue);
			query.executeUpdate();
		}
		
		BeanDescriptor beanDescriptor = new BeanDescriptor(fieldBean.getClass());

		for (PropertyDescriptor propertyDescriptor: beanDescriptor.getPropertyDescriptors()) {
			String fieldName = propertyDescriptor.getDisplayName();
			Object fieldValue = propertyDescriptor.getPropertyValue(fieldBean);
			InputSpec fieldSpec = issue.getProject().getIssueWorkflow().getField(fieldName);
			if (fieldSpec != null) {
				long ordinal = -1;
				
				if (fieldValue != null) {
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
							List<String> choices = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(false).keySet());
							ordinal = choices.indexOf(fieldValue);
						} finally {
							OneContext.pop();
						}
					} else if (fieldSpec instanceof NumberInput) {
						ordinal = Long.parseLong((String)fieldValue);
					} else if (fieldSpec instanceof DateInput) {
						ordinal = ((Date)fieldValue).getTime();
					}
				}

				IssueField field = new IssueField();
				field.setIssue(issue);
				field.setName(fieldName);
				field.setOrdinal(ordinal);
				field.setType(EditableUtils.getDisplayName(fieldSpec.getClass()));
				field.setCollected(fieldNames.contains(fieldName));
				
				if (fieldValue != null) {
					List<String> strings = fieldSpec.convertToStrings(fieldValue);
					if (!strings.isEmpty()) {
						for (String string: strings) {
							IssueField cloned = (IssueField) SerializationUtils.clone(field);
							cloned.setIssue(issue);
							cloned.setValue(string);
							save(cloned);
						}
					} else {
						save(field);
					}
				} else {
					save(field);
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

	@Sessional
	@Override
	public void populateFields(List<Issue> issues) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		CriteriaQuery<IssueField> query = builder.createQuery(IssueField.class);
		
		Root<IssueField> root = query.from(IssueField.class);
		query.select(root);
		root.join("issue");
		
		Expression<String> issueExpr = root.get("issue");
		query.where(issueExpr.in(issues));
		
		for (Issue issue: issues)
			issue.setFields(new ArrayList<>());
		
		for (IssueField field: getSession().createQuery(query).getResultList())
			field.getIssue().getFields().add(field);
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Map<String, String> getInvalidFields(Project project) {
		Query query = getSession().createQuery("select distinct name, type from IssueField where issue.project=:project");
		query.setParameter("project", project);
		Map<String, String> invalidFields = new HashMap<>();
		for (Object[] row: (List<Object[]>)query.getResultList()) {
			String name = (String) row[0];
			String type = (String) row[1];
			InputSpec fieldSpec = project.getIssueWorkflow().getField(name);
			if (fieldSpec == null || !EditableUtils.getDisplayName(fieldSpec.getClass()).equals(type))
				invalidFields.put(name, type);
		}
		return invalidFields;
	}

	@Transactional
	@Override
	public void fixInvalidFields(Project project, Map<String, InvalidFieldResolution> resolutions) {
		for (Map.Entry<String, InvalidFieldResolution> entry: resolutions.entrySet()) {
			Query query;
			if (entry.getValue().getFixType() == InvalidFieldResolution.FixType.CHANGE_TO_ANOTHER_FIELD) {
				query = getSession().createQuery("update IssueField set name=:newName where name=:oldName and issue.id in (select id from Issue where project=:project)");
				query.setParameter("oldName", entry.getKey());
				query.setParameter("newName", entry.getValue().getNewField());
			} else {
				query = getSession().createQuery("delete from IssueField where name=:fieldName and issue.id in (select id from Issue where project=:project)");
				query.setParameter("fieldName", entry.getKey());
			}
			query.setParameter("project", project);
			query.executeUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	@Sessional
	@Override
	public Map<String, String> getUndefinedFieldValues(Project project) {
		Query query = getSession().createQuery("select distinct name, value from IssueField where issue.project=:project and (type=:choice or type=:multiChoice)");
		query.setParameter("project", project);
		query.setParameter("choice", InputSpec.CHOICE);
		query.setParameter("multiChoice", InputSpec.MULTI_CHOICE);
		Map<String, String> undefinedFieldValues = new HashMap<>();
		OneContext.push(new OneContext() {

			@Override
			public Project getProject() {
				return project;
			}

			@Override
			public EditContext getEditContext(int level) {
				return new EditContext() {

					@Override
					public Object getInputValue(String name) {
						return null;
					}
					
				};
			}

			@Override
			public InputContext getInputContext() {
				throw new UnsupportedOperationException();
			}
			
		});
		try {
			for (Object[] row: (List<Object[]>)query.getResultList()) {
				String name = (String) row[0];
				String value = (String) row[1];
				InputSpec fieldSpec = project.getIssueWorkflow().getField(name);
				if (fieldSpec != null) {
					List<String> choices;
					if (fieldSpec instanceof ChoiceInput)
						choices = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
					else
						choices = new ArrayList<>(((MultiChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
					if (!choices.contains(value))
						undefinedFieldValues.put(name, value);
				}
			}
			return undefinedFieldValues;
		} finally {
			OneContext.pop();
		}
	}

	@Transactional
	@Override
	public void fixUndefinedFieldValues(Project project, Map<UndefinedFieldValue, UndefinedFieldValueResolution> resolutions) {
		for (Map.Entry<UndefinedFieldValue, UndefinedFieldValueResolution> entry: resolutions.entrySet()) {
			Query query;
			if (entry.getValue().getFixType() == UndefinedFieldValueResolution.FixType.CHANGE_TO_ANOTHER_VALUE) {
				query = getSession().createQuery("update IssueField set value=:newValue where name=:fieldName and value=:oldValue and issue.id in (select id from Issue where project=:project)");
				query.setParameter("fieldName", entry.getKey().getFieldName());
				query.setParameter("oldValue", entry.getKey().getFieldValue());
				query.setParameter("newValue", entry.getValue().getNewValue());
			} else {
				query = getSession().createQuery("delete from IssueField where name=:fieldName and value=:fieldValue and issue.id in (select id from Issue where project=:project)");
				query.setParameter("fieldName", entry.getKey().getFieldName());
				query.setParameter("fieldValue", entry.getKey().getFieldValue());
			}
			query.setParameter("project", project);
			query.executeUpdate();
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public void fixFieldValueOrders(Project project) {
		OneContext.push(new OneContext() {

			@Override
			public Project getProject() {
				return project;
			}

			@Override
			public EditContext getEditContext(int level) {
				return new EditContext() {

					@Override
					public Object getInputValue(String name) {
						return null;
					}
					
				};
			}

			@Override
			public InputContext getInputContext() {
				throw new UnsupportedOperationException();
			}
			
		});
		try {
			Query query = getSession().createQuery("select distinct name, value, ordinal from IssueField where issue.project=:project and type=:choice");
			query.setParameter("project", project);
			query.setParameter("choice", InputSpec.CHOICE);

			for (Object[] row: (List<Object[]>)query.getResultList()) {
				String name = (String) row[0];
				String value = (String) row[1];
				long ordinal = (long) row[2];
				InputSpec fieldSpec = project.getIssueWorkflow().getField(name);
				if (fieldSpec != null) {
					List<String> choices = new ArrayList<>(((ChoiceInput)fieldSpec).getChoiceProvider().getChoices(true).keySet());
					long newOrdinal = choices.indexOf(value);
					if (ordinal != newOrdinal) {
						query = getSession().createQuery("update IssueField set ordinal=:newOrdinal where name=:fieldName and value=:fieldValue and issue.id in (select id from Issue where project=:project)");
						query.setParameter("fieldName", name);
						query.setParameter("fieldValue", value);
						query.setParameter("newOrdinal", newOrdinal);
						query.setParameter("project", project);
						query.executeUpdate();
					}
				}
			}
		} finally {
			OneContext.pop();
		}
		
	}
	
}
