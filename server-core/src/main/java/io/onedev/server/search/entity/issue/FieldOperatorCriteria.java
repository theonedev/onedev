package io.onedev.server.search.entity.issue;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneException;
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
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		Path<?> attribute = field.get(IssueField.ATTR_VALUE);
		if (operator == IssueQueryLexer.IsEmpty) {
			return builder.isNull(attribute);
		} else if (operator == IssueQueryLexer.IsMe) {
			if (User.get() != null)
				return builder.equal(attribute, User.get().getName());
			else
				throw new OneException("Please login to perform this query");
		} else {
			Build build = Build.get();
			if (build != null) {
				if (operator == IssueQueryLexer.IsCurrent) { 
					return builder.equal(attribute, String.valueOf(build.getNumber()));
				} else {
					Collection<Long> numbersOfStreamPrevious = build.getNumbersOfStreamPrevious(EntityCriteria.IN_CLAUSE_LIMIT);
					if (!numbersOfStreamPrevious.isEmpty())
						return attribute.in(numbersOfStreamPrevious.stream().map(it->it.toString()).collect(Collectors.toSet()));
					else
						return builder.disjunction();
				}
			} else {
				throw new OneException("No build in query context");
			}
		}
	}

	@Override
	public boolean matches(Issue issue) {
		Object fieldValue = issue.getFieldValue(getFieldName());
		if (operator == IssueQueryLexer.IsEmpty) {
			return fieldValue == null;
		} else if (operator == IssueQueryLexer.IsMe) {
			if (User.get() != null)
				return Objects.equals(fieldValue, User.get().getName());
			else
				throw new OneException("Please login to perform this query");
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
				throw new OneException("No build in query context");
			}
		}
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
