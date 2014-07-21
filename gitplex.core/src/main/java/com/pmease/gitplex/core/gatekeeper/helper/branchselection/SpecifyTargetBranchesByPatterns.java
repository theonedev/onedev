package com.pmease.gitplex.core.gatekeeper.helper.branchselection;

import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.gatekeeper.IfSubmitToSpecifiedBranchPatterns;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.editable.annotation.OmitName;

@SuppressWarnings("serial")
@Editable(order=200, name="Specify Branch Patterns")
public class SpecifyTargetBranchesByPatterns implements TargetBranchSelection {

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
	public GateKeeper getGateKeeper() {
		IfSubmitToSpecifiedBranchPatterns gateKeeper = new IfSubmitToSpecifiedBranchPatterns();
		gateKeeper.setBranchPatterns(branchPatterns);
		return gateKeeper;
	}

}
