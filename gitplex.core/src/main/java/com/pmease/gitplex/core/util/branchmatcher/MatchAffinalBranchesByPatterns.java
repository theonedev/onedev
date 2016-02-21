package com.pmease.gitplex.core.util.branchmatcher;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.util.match.WildcardUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.OmitName;
import com.pmease.gitplex.core.model.Depot;

@SuppressWarnings("serial")
@Editable(order=200, name="Specify Branch Patterns")
public class MatchAffinalBranchesByPatterns implements AffinalBranchMatcher {

	private String branchPatterns;
	
	@Editable(name="Branch Patterns", description=
			"Patterns should be specified following the <a href='http://wiki.pmease.com/display/gp/Pattern+Set'>pattern set</a> format. "
			+ "When matching a branch agains the patterns, the fully qualified name will be used, which will be "
			+ "&lt;user name&gt;/&lt;repository name&gt;:&lt;branch name&gt;")
	@OmitName
	@NotEmpty
	public String getBranchPatterns() {
		return branchPatterns;
	}

	public void setBranchPatterns(String branchPatterns) {
		this.branchPatterns = branchPatterns;
	}

	@Override
	public boolean matches(Depot depot, String branch) {
		return WildcardUtils.matchPath(getBranchPatterns(), depot.getRevisionFQN(branch));
	}

	@Override
	public Object trim(Object context) {
		return this;
	}

}
