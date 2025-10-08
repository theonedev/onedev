package io.onedev.server.buildspec.job.action.condition;

import static io.onedev.server.buildspec.job.action.condition.ActionCondition.getRuleName;
import static io.onedev.server.model.Build.NAME_TAG;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.model.Build;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class TagCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String tag;
	
	private final int operator;
	
	public TagCriteria(String tag, int operator) {
		this.tag = tag;
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		var matches = build.getTag() != null && new PathMatcher().matches(tag, build.getTag());
		if (operator == ActionConditionLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_TAG) + " " + getRuleName(operator) + " " + quote(tag);
	}
	
}
