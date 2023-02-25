package io.onedev.server.buildspec.job.action.condition;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.model.Build;
import io.onedev.server.util.criteria.Criteria;

public class OnBranchCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;
	
	private final String branch;
	
	public OnBranchCriteria(String branch) {
		this.branch = branch;
	}
	
	@Override
	public Predicate getPredicate(CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		return build.getProject().isCommitOnBranch(build.getCommitId(), branch);
	}

	@Override
	public String toStringWithoutParens() {
		return ActionCondition.getRuleName(ActionConditionLexer.OnBranch) + " "
				+ quote(branch);
	}
	
}
