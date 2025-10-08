package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class MergeStrategyCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final MergeStrategy value;
	
	private final int operator;
	
	public MergeStrategyCriteria(MergeStrategy value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(PullRequest.PROP_MERGE_STRATEGY);
		var predicate = builder.equal(attribute, value);
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		var matches = request.getMergeStrategy() == value;
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_MERGE_STRATEGY) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value.toString());
	}

}
