package io.onedev.server.util.jobmatch;

import static io.onedev.server.util.jobmatch.JobMatch.getRuleName;
import static io.onedev.server.util.jobmatch.JobMatchLexer.Is;
import static io.onedev.server.util.query.BuildQueryConstants.FIELD_JOB;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;


public class NameCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private String jobName;
	
	public NameCriteria(String jobName) {
		this.jobName = jobName;
	}

	@Override
	public boolean matches(Build build) {
		return WildcardUtils.matchString(jobName, build.getJobName());
	}

	@Override
	public String asString() {
		return quote(FIELD_JOB) + " " + getRuleName(Is) + " " + quote(jobName);
	}
	
}
