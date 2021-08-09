package io.onedev.server.job.match;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

import static io.onedev.server.job.match.JobMatch.getRuleName;
import static io.onedev.server.job.match.JobMatchLexer.OnBranch;

public class BranchCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String branch;
	
	public BranchCriteria(String branch) {
		this.branch = branch;
	}
	
	@Override
	public boolean matches(Build build) {
		return build.getProject().isCommitOnBranches(build.getCommitId(), branch);
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(OnBranch) + " " + quote(branch);
	}

}
