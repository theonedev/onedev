package io.onedev.server.job.match;

import io.onedev.server.util.criteria.Criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class NonPullRequestCommitsCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean matches(JobMatchContext context) {
		return context.getCommitId() != null;
	}

	@Override
	public String toStringWithoutParens() {
		return "non-pull-request commits";
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
