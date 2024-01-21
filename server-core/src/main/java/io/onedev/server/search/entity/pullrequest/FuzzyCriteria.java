package io.onedev.server.search.entity.pullrequest;

import io.onedev.commons.utils.StringUtils;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.criteria.OrCriteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class FuzzyCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public FuzzyCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return parse(value).getPredicate(query, from, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return parse(value).matches(request);
	}
	
	private Criteria<PullRequest> parse(String value) {
		return new OrCriteria<>(
				new TitleCriteria(value),
				new DescriptionCriteria(value),
				new CommentCriteria(value));
	}

	@Override
	public String toStringWithoutParens() {
		return "~" + StringUtils.escape(value, "~") + "~";
	}

}
