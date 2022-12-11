package io.onedev.server.job.authorization;

import static io.onedev.server.job.authorization.JobAuthorization.getRuleName;
import static io.onedev.server.job.authorization.JobAuthorizationLexer.Is;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.job.authorization.JobAuthorization.Context;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class JobCriteria extends Criteria<Context> {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	public JobCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public boolean matches(Context context) {
		return WildcardUtils.matchString(jobName, context.getJobName());
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Context, Context> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_JOB) + " " + getRuleName(Is) + " " + quote(jobName);
	}

}
