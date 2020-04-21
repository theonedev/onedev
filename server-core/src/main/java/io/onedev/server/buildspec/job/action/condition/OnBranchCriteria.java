package io.onedev.server.buildspec.job.action.condition;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class OnBranchCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String branch;
	
	public OnBranchCriteria(String branch) {
		this.branch = branch;
	}
	
	@Override
	public boolean matches(Build build) {
		return build.getProject().isCommitOnBranches(build.getCommitId(), branch);
	}

	@Override
	public String toStringWithoutParens() {
		return ActionCondition.getRuleName(ActionConditionLexer.OnBranch) + " "
				+ quote(branch);
	}
	
}
