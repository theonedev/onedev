package io.onedev.server.job.requirement;

import static io.onedev.server.job.requirement.JobRequirement.getRuleName;
import static io.onedev.server.job.requirement.JobRequirementLexer.OnBranch;

import io.onedev.server.util.ProjectAndBranch;
import io.onedev.server.util.criteria.Criteria;
import io.onedev.server.util.match.WildcardUtils;

public class BranchCriteria extends Criteria<ProjectAndBranch> {

	private static final long serialVersionUID = 1L;
	
	private final String branch;
	
	public BranchCriteria(String branch) {
		this.branch = branch;
	}
	
	@Override
	public boolean matches(ProjectAndBranch projectAndBranch) {
		return WildcardUtils.matchPath(branch, projectAndBranch.getBranch());
	}

	@Override
	public String toStringWithoutParens() {
		return getRuleName(OnBranch) + " " + quote(branch);
	}

}
