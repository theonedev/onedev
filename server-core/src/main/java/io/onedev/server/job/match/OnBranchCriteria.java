package io.onedev.server.job.match;

import static io.onedev.commons.utils.match.WildcardUtils.matchPath;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.onedev.server.util.ProjectScope;
import io.onedev.server.util.criteria.Criteria;

public class OnBranchCriteria extends Criteria<JobMatchContext> {

	private static final long serialVersionUID = 1L;
	
	private final String branch;
	
	public OnBranchCriteria(String branch) {
		this.branch = branch;
	}
	
	@Override
	public boolean matches(JobMatchContext context) {
		if (context.getBranch() != null) 
			return matchPath(branch, context.getBranch());
		else if (context.getCommitId() != null) 
			return context.getProject().isCommitOnBranch(context.getCommitId(), branch);
		else
			return matchPath(branch, "main");
	}

	@Override
	public String toStringWithoutParens() {
		return JobMatch.getRuleName(JobMatchLexer.OnBranch) + " " + quote(branch);
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<JobMatchContext, JobMatchContext> from,
			CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
}
