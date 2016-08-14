package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.match.PatternMatcher;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.annotation.RefMatch;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.gatekeeper.checkresult.GateCheckResult;
import com.pmease.gitplex.core.util.ChildAwareMatcher;
import com.pmease.gitplex.core.util.ParentAwareMatcher;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeParser;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@Editable(order=100, icon="fa-code-fork", description=
		"This gate keeper will be passed if specified refs is being touched")
public class IfTouchSpecifiedRefs extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private static final PatternMatcher INCLUDE_MATCHER = new ParentAwareMatcher();
	
	private static final PatternMatcher EXCLUDE_MATCHER = new ChildAwareMatcher();
	
	private String refMatch;
	
	@Editable(name="Refs to Match", order=100)
	@RefMatch
	@NotEmpty
	public String getRefMatch() {
		return refMatch;
	}

	public void setRefMatch(String refMatch) {
		this.refMatch = refMatch;
	}

	private GateCheckResult doCheck(String refName) {
		if (IncludeExcludeUtils.getIncludeExclude(refMatch).matches(INCLUDE_MATCHER, EXCLUDE_MATCHER, refName)) {
			return passed(Lists.newArrayList("Target ref matches '" + refMatch + "'"));
		} else {
			return failed(Lists.newArrayList("Target ref does not match '" + refMatch + "'"));
		}
	}
	
	@Override
	public GateCheckResult doCheckRequest(PullRequest request) {
		return doCheck(request.getTargetRef());
	}

	@Override
	protected GateCheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		return doCheck(GitUtils.branch2ref(branch));
	}

	@Override
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		return doCheck(refName);
	}

	@Override
	public boolean onRefDelete(String refName) {
		StringBuilder builder = new StringBuilder();
		for (IncludeExcludeParser.CriteriaContext criteriaContext: IncludeExcludeUtils.parse(refMatch).criteria()) {
			if (criteriaContext.includeMatch() != null) { 
				String value = IncludeExcludeUtils.getValue(criteriaContext.includeMatch().Value());
				if (!value.equals(refName))
					builder.append(String.format("include(%s) ", value));
			} else {
				String value = IncludeExcludeUtils.getValue(criteriaContext.excludeMatch().Value());
				if (!value.equals(refName))
					builder.append(String.format("exclude(%s) ", value));
			}
		}
		refMatch = builder.toString().trim();
		return refMatch.length() == 0;
	}

}
