package com.pmease.gitplex.core.branchmatcher;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.model.Branch;

@SuppressWarnings("serial")
@Editable(order=200, name="Specify Branch Patterns")
public class MatchGlobalBranchesByPatterns implements GlobalBranchMatcher {

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
	public Object trim(Object context) {
		return this;
	}

	@Override
	public boolean matches(Branch branch) {
		return WildcardUtils.matchPath(getBranchPatterns(), branch.getFullName());
	}

}
