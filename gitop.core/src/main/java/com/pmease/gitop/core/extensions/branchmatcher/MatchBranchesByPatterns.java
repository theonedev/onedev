package com.pmease.gitop.core.extensions.branchmatcher;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.helper.BranchMatcher;

@SuppressWarnings("serial")
@Editable(order=200, name="Specify Branch Patterns")
public class MatchBranchesByPatterns implements BranchMatcher {

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
	public Object trim(Object context) {
		return this;
	}

	@Override
	public boolean matches(Branch branch) {
		return WildcardUtils.matchPath(getBranchPatterns(), branch.getName());
	}

}
