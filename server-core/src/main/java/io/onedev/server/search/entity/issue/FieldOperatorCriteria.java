package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.SecurityUtils;

public class FieldOperatorCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;

	public FieldOperatorCriteria(String name, int operator) {
		super(name);
		this.operator = operator;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder, User user) {
		Path<?> attribute = field.get(IssueField.ATTR_VALUE);
		if (operator == IssueQueryLexer.IsEmpty) {
			return builder.isNull(attribute);
		} else if (operator == IssueQueryLexer.IsMe) {
			if (user != null)
				return builder.equal(attribute, user.getName());
			else
				return builder.disjunction();
		} else {
			Build build = Build.get();
			if (build != null) {
				if (operator == IssueQueryLexer.IsCurrent) { 
					return builder.equal(attribute, build.getNumber());
				} else {
					Collection<Long> numbersOfStreamPrevious = build.getNumbersOfStreamPrevious(EntityCriteria.IN_CLAUSE_LIMIT);
					if (!numbersOfStreamPrevious.isEmpty())
						return attribute.in(numbersOfStreamPrevious.stream().map(it->it.toString()).collect(Collectors.toSet()));
					else
						return builder.disjunction();
				}
			} else {
				return builder.disjunction();
			}
		}
	}

	@Override
	public boolean matches(Issue issue, User user) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsEmpty) {
			return fieldValue == null;
		} else if (operator == IssueQueryLexer.IsMe) {
			if (user != null)
				return Objects.equals(fieldValue, user.getName());
			else
				return false;
		} else {
			Build build = Build.get();
			if (build != null) { 
				if (operator == IssueQueryLexer.IsCurrent) { 
					return build.getId().toString().equals(fieldValue);
				} else { 
					return build.getNumbersOfStreamPrevious(EntityCriteria.IN_CLAUSE_LIMIT)
							.stream()
							.anyMatch(it->it.equals(fieldValue));
				}
			} else { 
				return false;
			}
		}
	}

	@Override
	public boolean needsLogin() {
		return operator == IssueQueryLexer.IsMe;
	}

	@Override
	public String toString() {
		return IssueQuery.quote(getFieldName()) + " " + IssueQuery.getRuleName(operator);
	}

	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (operator == IssueQueryLexer.IsEmpty)
			issue.setFieldValue(getFieldName(), null);
		else if (operator == IssueQueryLexer.IsMe)
			issue.setFieldValue(getFieldName(), SecurityUtils.getUser().getName());
	}

}
