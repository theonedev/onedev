package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.support.administration.GlobalIssueSetting;
import io.onedev.server.model.support.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValue;
import io.onedev.server.web.component.issue.workflowreconcile.UndefinedFieldValuesResolution;

public class ChoiceFieldCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;

	private String value;
	
	private final long ordinal;
	
	private final int operator;
	
	private final boolean allowMultiple;
	
	public ChoiceFieldCriteria(String name, String value, long ordinal, int operator, boolean allowMultiple) {
		super(name);
		this.value = value;
		this.ordinal = ordinal;
		this.operator = operator;
		this.allowMultiple = allowMultiple;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		if (allowMultiple)
			return builder.equal(field.get(IssueField.PROP_VALUE), value);
		else if (operator == IssueQueryLexer.Is) 
			return builder.equal(field.get(IssueField.PROP_VALUE), value);
		else if (operator == IssueQueryLexer.IsGreaterThan) 
			return builder.greaterThan(field.get(IssueField.PROP_ORDINAL), ordinal);
		else
			return builder.lessThan(field.get(IssueField.PROP_ORDINAL), ordinal);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (allowMultiple) 
			return ((List<String>)fieldValue).contains(value);
		else if (operator == IssueQueryLexer.Is)
			return Objects.equals(fieldValue, value);
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getFieldOrdinal(getFieldName(), (String)fieldValue) > ordinal;
		else
			return issue.getFieldOrdinal(getFieldName(), (String)fieldValue) < ordinal;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(getFieldName()) + " " 
				+ IssueQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		SpecifiedChoices specifiedChoices = SpecifiedChoices.of(issueSetting.getFieldSpec(getFieldName()));
		if (specifiedChoices != null && !specifiedChoices.getChoiceValues().contains(value)) 
			undefinedFieldValues.add(new UndefinedFieldValue(getFieldName(), value));
		return undefinedFieldValues;
	}
	
	@Override
	public boolean fixUndefinedFieldValues(Map<String, UndefinedFieldValuesResolution> resolutions) {
		for (Map.Entry<String, UndefinedFieldValuesResolution> entry: resolutions.entrySet()) {
			if (entry.getKey().equals(getFieldName())) {
				if (entry.getValue().getDeletions().contains(value))
					return false;
				String newValue = entry.getValue().getRenames().get(value);
				if (newValue != null)
					value = newValue;
			}
		}
		return true;
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void fill(Issue issue) {
		if (allowMultiple) {
			List<String> valueFromIssue = (List<String>) issue.getFieldValue(getFieldName());
			if (valueFromIssue == null)
				valueFromIssue = new ArrayList<>();
			valueFromIssue.add(value);
			issue.setFieldValue(getFieldName(), valueFromIssue);
		} else {
			issue.setFieldValue(getFieldName(), value);
		}
	}

}
