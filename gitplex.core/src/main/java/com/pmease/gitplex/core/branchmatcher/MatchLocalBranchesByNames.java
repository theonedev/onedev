package com.pmease.gitplex.core.branchmatcher;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.OmitName;
import com.pmease.gitplex.core.editable.BranchChoice;

@SuppressWarnings("serial")
@Editable(name="Specify Branch Names", order=100)
public class MatchLocalBranchesByNames implements LocalBranchMatcher {

	private List<String> branches = new ArrayList<>();
	
	@Editable(name="Branch Names")
	@BranchChoice
	@OmitName
	@NotNull
	@Size(min=1, message="At least one branch has to be selected.")
	public List<String> getBranches() {
		return branches;
	}

	public void setBranches(List<String> branches) {
		this.branches = branches;
	}

	@Override
	public boolean matches(String branch) {
		for (String each: getBranches()) {
			if (each.equals(branch))
				return true;
		}
		return false;
	}

}
