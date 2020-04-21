package io.onedev.server.util.jobmatch;

import static io.onedev.server.util.jobmatch.JobMatch.getRuleName;
import static io.onedev.server.util.jobmatch.JobMatchLexer.All;

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
