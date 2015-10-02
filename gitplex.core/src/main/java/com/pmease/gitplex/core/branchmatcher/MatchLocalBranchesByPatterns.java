package com.pmease.gitplex.core.branchmatcher;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.OmitName;

@SuppressWarnings("serial")
@Editable(order=200, name="Specify Branch Patterns")
public class MatchLocalBranchesByPatterns implements LocalBranchMatcher {

	private String branchPatterns;
	
	@Editable(name="Branch Patterns", description=
			"Patterns should be specified following the <a href='http://wiki.pmease.com/display/gp/Pattern+Set'>pattern set</a> format.")
	@OmitName
	@NotEmpty
	public String getBranchPatterns() {
		return branchPatterns;
	}

	public void setBranchPatterns(String branchPatterns) {
		this.branchPatterns = branchPatterns;
	}

	@Override
	public boolean matches(String branch) {
		return WildcardUtils.matchPath(getBranchPatterns(), branch);
	}

}
