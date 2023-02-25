package io.onedev.server.job.match;

import io.onedev.commons.utils.ExplicitException;
import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class JobCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	public JobCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public boolean matches(JobMatchContext context) {
		if (context.getJobName() != null)
			return WildcardUtils.matchString(jobName, context.getJobName());
		else 
			throw new ExplicitException("Job name not available in match context");
	}

	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toStringWithoutParens() {
		return quote(Build.NAME_JOB) + " " + JobMatch.getRuleName(JobMatchLexer.Is) + " " + quote(jobName);
	}

}
