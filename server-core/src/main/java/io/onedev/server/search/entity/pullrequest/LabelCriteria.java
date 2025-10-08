package io.onedev.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.LabelSpec;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestLabel;
import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class LabelCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final LabelSpec labelSpec;
	
	private int operator;
	
	public LabelCriteria(LabelSpec labelSpec, int operator) {
		this.labelSpec = labelSpec;
		this.operator = operator;
	}

	public LabelSpec getLabelSpec() {
		return labelSpec;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		var labelQuery = query.subquery(PullRequestLabel.class);
		var labelRoot = labelQuery.from(PullRequestLabel.class);
		labelQuery.select(labelRoot);

		var predicate = builder.exists(labelQuery.where(
				builder.equal(labelRoot.get(PullRequestLabel.PROP_REQUEST), from), 
				builder.equal(labelRoot.get(PullRequestLabel.PROP_SPEC), labelSpec)));
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		var matches = request.getLabels().stream().anyMatch(it->it.getSpec().equals(labelSpec));
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return Criteria.quote(PullRequest.NAME_LABEL) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ Criteria.quote(labelSpec.getName());
	}

}
