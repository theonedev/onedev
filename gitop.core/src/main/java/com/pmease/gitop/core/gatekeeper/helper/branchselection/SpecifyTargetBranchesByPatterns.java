package com.pmease.gitop.core.gatekeeper.helper.branchselection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.Editable;
import com.pmease.commons.editable.OmitName;
import com.pmease.gitop.core.gatekeeper.IfSubmitToSpecifiedBranchPatterns;
import com.pmease.gitop.model.gatekeeper.GateKeeper;

@SuppressWarnings("serial")
@Editable(order=200, name="Specify Branch Patterns")
public class SpecifyTargetBranchesByPatterns implements TargetBranchSelection {

	private String branchPatterns;
	
	@Editable(name="Branch Patterns", description="Specify branch patterns to match. Below is some examples:"
			+ "<ul>"
			+ "<li><i>dev/*</i>: matches all branches directly under dev."
			+ "<li><i>dev/**</i>: matches all branches under dev recursively."
			+ "<li><i>**</i>: matches all branches."
			+ "<li><i>**/bugfix</i>: matches all branches whose last segment is bugfix."
			+ "<li><i>-dev/**, **</i>: matches all branches except those under dev."
			+ "</ul>")
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
