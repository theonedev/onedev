package io.onedev.server.buildspec.job.action.condition;

import java.util.function.Predicate;

import io.onedev.server.model.Build;

public class OnBranchCriteria implements Predicate<Build> {

	private final String branch;
	
	public OnBranchCriteria(String branch) {
		this.branch = branch;
	}
	
	@Override
	public boolean test(Build build) {
		return build.getProject().isCommitOnBranches(build.getCommitId(), branch);
	}

}
