package io.onedev.server.search.entity.issue;

import io.onedev.server.model.Issue;
import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class SimpleNumberCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final long value;
	
	public SimpleNumberCriteria(long value) {
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		return builder.equal(from.get(Issue.PROP_NUMBER), value);
	}

	@Override
	public boolean matches(Issue issue) {
		return issue.getNumber() == value;
	}

	@Override
	public String toStringWithoutParens() {
		return "#" + value;
	}

}
