package com.gitplex.server.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.gitplex.server.util.includeexclude.IncludeExcludeParser;
import com.gitplex.server.entity.Account;
import com.gitplex.server.entity.Depot;
import com.gitplex.server.entity.PullRequest;
import com.gitplex.server.gatekeeper.checkresult.GateCheckResult;
import com.gitplex.server.git.GitUtils;
import com.gitplex.server.util.editable.annotation.Editable;
import com.gitplex.server.util.editable.annotation.RefMatch;
import com.gitplex.server.util.includeexclude.IncludeExcludeUtils;
import com.gitplex.server.util.match.ChildAwareMatcher;
import com.gitplex.server.util.match.ParentAwareMatcher;
import com.gitplex.server.util.match.PatternMatcher;

@Editable(order=100, icon="fa-code-fork", description=
		"This gatekeeper will be passed if specified refs is being touched")
public class TouchSpecifiedRefs extends AbstractGateKeeper {

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
	protected GateCheckResult doCheckPush(Account user, Depot depot, String refName, 
			ObjectId oldObjectId, ObjectId newObjectId) {
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
