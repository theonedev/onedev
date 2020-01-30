package io.onedev.server.search.entity.issue;

import java.nio.channels.IllegalSelectorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.OneException;
import io.onedev.server.issue.fieldspec.BuildChoiceField;
import io.onedev.server.issue.fieldspec.CommitField;
import io.onedev.server.issue.fieldspec.PullRequestChoiceField;
import io.onedev.server.model.Build;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueField;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.search.entity.EntityCriteria;
import io.onedev.server.util.ProjectAwareCommit;
import io.onedev.server.util.SecurityUtils;
import io.onedev.server.util.query.IssueQueryConstants;

public class FieldOperatorCriteria extends FieldCriteria {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	private final boolean allowMultiple;

	public FieldOperatorCriteria(String name, int operator, boolean allowMultiple) {
		super(name);
		this.operator = operator;
		this.allowMultiple = allowMultiple;
	}

	@Override
	protected Predicate getValuePredicate(Join<?, ?> field, CriteriaBuilder builder) {
		Path<?> valueAttribute = field.get(IssueField.ATTR_VALUE);
		Path<?> projectAttribute = field.getParent().get(IssueQueryConstants.ATTR_PROJECT);		
		if (operator == IssueQueryLexer.IsEmpty) {
			return null;
		} else if (operator == IssueQueryLexer.IsMe) {
			if (User.get() != null)
				return builder.equal(valueAttribute, User.get().getName());
			else
				throw new OneException("Please login to perform this query");
		} else if (operator == IssueQueryLexer.IsCurrent) {
			if (getFieldSpec() instanceof BuildChoiceField) {
				Build build = Build.get();
				if (build != null) { 
					return builder.and(
							builder.equal(projectAttribute, build.getProject()),
							builder.equal(valueAttribute, String.valueOf(build.getNumber())));
				} else {
					throw new OneException("No current build in query context");
				}
			} else if (getFieldSpec() instanceof PullRequestChoiceField) {
				PullRequest request = PullRequest.get();
				if (request != null) {
					return builder.and(
							builder.equal(projectAttribute, request.getTargetProject()),
							builder.equal(valueAttribute, String.valueOf(request.getNumber())));
				} else {
					throw new OneException("No current pull request in query context");
				}
			} else if (getFieldSpec() instanceof CommitField) {
				ProjectAwareCommit commit = ProjectAwareCommit.get();
				if (commit != null) {
					return builder.and(
							builder.equal(projectAttribute, commit.getProject()),
							builder.equal(valueAttribute, commit.getCommitId().name()));
				} else {
					throw new OneException("No current commit in query context");
				}
			} else {
				throw new IllegalStateException();
			}
		} else if (operator == IssueQueryLexer.IsPrevious) {
			if (getFieldSpec() instanceof BuildChoiceField) {
				Build build = Build.get();
				if (build != null) { 
					Collection<Long> numbersOfStreamPrevious = Build.get().getNumbersOfStreamPrevious(EntityCriteria.IN_CLAUSE_LIMIT);
					if (!numbersOfStreamPrevious.isEmpty()) {
						return builder.and(
								builder.equal(projectAttribute, build.getProject()),
								valueAttribute.in(numbersOfStreamPrevious.stream().map(it->it.toString()).collect(Collectors.toSet())));
					} else {
						return builder.equal(projectAttribute, build.getProject());
					}
				} else {
					throw new OneException("No current build in query context");
				}
			} else {
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalStateException();
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
		} else if (operator == IssueQueryLexer.IsCurrent) {
			if (getFieldSpec() instanceof BuildChoiceField) {
				Build build = Build.get();
				if (build != null) 
					return build.getProject().equals(issue.getProject()) && build.getId().toString().equals(fieldValue);
				else  
					throw new OneException("No build in query context");
			} else if (getFieldSpec() instanceof PullRequestChoiceField) {
				PullRequest request = PullRequest.get();
				if (request != null) 
					return request.getTargetProject().equals(issue.getProject()) && request.getId().toString().equals(fieldValue);
				else  
					throw new OneException("No pull request in query context");
			} else if (getFieldSpec() instanceof CommitField) {
				ProjectAwareCommit commit = ProjectAwareCommit.get();
				if (commit != null) 
					return commit.getProject().equals(issue.getProject()) && commit.getCommitId().name().equals(fieldValue);
				else  
					throw new OneException("No commit in query context");
			} else {
				throw new IllegalStateException();
			}
		} else if (operator == IssueQueryLexer.IsPrevious) {
			if (getFieldSpec() instanceof BuildChoiceField) {
				Build build = Build.get();
				if (build != null) {
					return build.getProject().equals(issue.getProject()) 
							&& build.getNumbersOfStreamPrevious(EntityCriteria.IN_CLAUSE_LIMIT)
								.stream()
								.anyMatch(it->it.equals(fieldValue));
				} else {
					throw new OneException("No build in query context");
				}
			} else {
				throw new IllegalStateException();
			}
		} else {
			throw new IllegalSelectorException();
		}
	}

	@Override
	public String asString() {
		return quote(getFieldName()) + " " + IssueQuery.getRuleName(operator);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void fill(Issue issue, Set<String> initedLists) {
		if (operator == IssueQueryLexer.IsEmpty) {
			issue.setFieldValue(getFieldName(), null);
		} else if (operator == IssueQueryLexer.IsMe) {
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
				list.add(SecurityUtils.getUser().getName());
			} else {
				issue.setFieldValue(getFieldName(), SecurityUtils.getUser().getName());
			}
		}
	}

}
