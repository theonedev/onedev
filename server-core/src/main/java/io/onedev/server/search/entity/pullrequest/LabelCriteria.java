package io.onedev.server.search.entity.pullrequest;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.util.criteria.Criteria;

public class LabelCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	public LabelCriteria(LabelSpec labelSpec) {
		this.labelSpec = labelSpec;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(PullRequestLabel.class);
		var labelRoot = labelQuery.from(PullRequestLabel.class);
		labelQuery.select(labelRoot);

		return builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(PullRequestLabel.PROP_REQUEST), from), 
				builder.equal(labelRoot.get(PullRequestLabel.PROP_SPEC), labelSpec)));
	}

	@Override
	public boolean matches(PullRequest request) {
		return request.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(PullRequest.NAME_LABEL) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Is) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
