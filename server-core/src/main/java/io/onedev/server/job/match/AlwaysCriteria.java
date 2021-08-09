package io.onedev.server.job.match;

import static io.onedev.server.job.match.JobMatch.getRuleName;
import static io.onedev.server.job.match.JobMatchLexer.All;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class AlwaysCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean matches(Build build) {
		return true;
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(All);
	}

}
