package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.User;
import io.onedev.server.model.support.administration.IssueSetting;
import io.onedev.server.util.IssueConstants;

public abstract class FieldCriteria extends IssueCriteria {

	private static final long serialVersionUID = 1L;

	private String fieldName;
	
	public FieldCriteria(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	@Override
	public final Predicate getPredicate(Root<Issue> root, CriteriaBuilder builder, User user) {
		Join<?, ?> join = root.join(IssueConstants.ATTR_FIELDS, JoinType.LEFT);
		Predicate namePredicate = builder.equal(join.get(IssueField.ATTR_NAME), getFieldName());
		return builder.and(namePredicate, getValuePredicate(join, builder, user));
	}

	protected abstract Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder, User user);
	
	@Override
	public Collection<String> getUndefinedFields() {
		Set<String> undefinedFields = new HashSet<>();
		IssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		if (!IssueConstants.QUERY_FIELDS.contains(fieldName) 
				&& issueSetting.getFieldSpec(fieldName) == null) {
			undefinedFields.add(fieldName);
		}
		return undefinedFields;
	}

	@Override
	public void onRenameField(String oldField, String newField) {
		if (oldField.equals(fieldName))
			fieldName = newField;
	}

	@Override
	public boolean onDeleteField(String fieldName) {
		return fieldName.equals(this.fieldName);
	}
	
}
