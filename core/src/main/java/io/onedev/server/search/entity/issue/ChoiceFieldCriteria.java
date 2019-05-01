package io.onedev.server.search.entity.issue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueFieldEntity;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.setting.GlobalIssueSetting;
import io.onedev.server.search.entity.QueryBuildContext;
import io.onedev.server.util.ValueSetEdit;
import io.onedev.server.util.inputspec.choiceinput.choiceprovider.SpecifiedChoices;
import io.onedev.server.web.page.project.issueworkflowreconcile.UndefinedFieldValue;

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
	public Predicate getPredicate(Project project, QueryBuildContext<Issue> context, User user) {
		From<?, ?> join = context.getJoin(getFieldName());
		if (allowMultiple) {
			return context.getBuilder().equal(join.get(IssueFieldEntity.ATTR_VALUE), value);
		} else {
			if (operator == IssueQueryLexer.Is)
				return context.getBuilder().equal(join.get(IssueFieldEntity.ATTR_VALUE), value);
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return context.getBuilder().greaterThan(join.get(IssueFieldEntity.ATTR_ORDINAL), ordinal);
			else
				return context.getBuilder().lessThan(join.get(IssueFieldEntity.ATTR_ORDINAL), ordinal);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Issue issue, User user) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (allowMultiple) {
			return ((List<String>)fieldValue).contains(value);
		} else {
			if (operator == IssueQueryLexer.Is)
				return Objects.equals(fieldValue, value);
			else if (operator == IssueQueryLexer.IsGreaterThan)
				return issue.getFieldOrdinal(getFieldName(), fieldValue) > ordinal;
			else
				return issue.getFieldOrdinal(getFieldName(), fieldValue) < ordinal;
		}
	}

	@Override
	public boolean needsLogin() {
		return false;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator) + " " + IssueQuery.quote(value);
	}

	@Override
	public Collection<UndefinedFieldValue> getUndefinedFieldValues() {
		Collection<UndefinedFieldValue> undefinedFieldValues = new HashSet<>();
		GlobalIssueSetting issueSetting = OneDev.getInstance(SettingManager.class).getIssueSetting();
		SpecifiedChoices specifiedChoices = SpecifiedChoices.of(issueSetting.getFieldSpec(getFieldName()));
		if (specifiedChoices != null && !specifiedChoices.getChoiceValues().contains(value)) {
			undefinedFieldValues.add(new UndefinedFieldValue(getFieldName(), value));
		}
		return undefinedFieldValues;
	}
	
	@Override
	public boolean onEditFieldValues(String fieldName, ValueSetEdit valueSetEdit) {
		if (fieldName.equals(getFieldName())) {
			if (valueSetEdit.getDeletions().contains(value))
				return true;
			String newValue = valueSetEdit.getRenames().get(value);
			if (newValue != null)
				value = newValue;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (allowMultiple) {
			List list;
			if (!initedLists.contains(getFieldName())) {
				list = new ArrayList();
				issue.setFieldValue(getFieldName(), list);
				initedLists.add(getFieldName());
			} else {
				list = (List) issue.getFieldValue(getFieldName());
				if (list == null) {
					list = new ArrayList();
					issue.setFieldValue(getFieldName(), list);
				}
			}
			list.add(value);
		} else {
			issue.setFieldValue(getFieldName(), value);
		}
	}

}
