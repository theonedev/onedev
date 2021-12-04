package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergeStrategy;
import io.onedev.server.util.criteria.Criteria;

public class MergeStrategyCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private MergeStrategy value;
	
	public MergeStrategyCriteria(MergeStrategy value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<?> attribute = from.get(PullRequest.PROP_MERGE_STRATEGY);
		return builder.equal(attribute, value);
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getMergeStrategy() == value;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_MERGE_STRATEGY) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ quote(value.toString());
	}

}
