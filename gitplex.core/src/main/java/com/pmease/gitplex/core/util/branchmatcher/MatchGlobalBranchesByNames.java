package com.pmease.gitplex.core.util.branchmatcher;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.OmitName;
import com.pmease.gitplex.core.model.DepotAndBranch;
import com.pmease.gitplex.core.util.editable.BranchChoice;
import com.pmease.gitplex.core.util.editable.BranchChoice.Scope;
import com.pmease.gitplex.core.model.Depot;

@SuppressWarnings("serial")
@Editable(name="Specify Branch Names", order=100)
public class MatchGlobalBranchesByNames implements GlobalBranchMatcher {

	private List<String> depotAndBranches = new ArrayList<>();
	
	@Editable(name="Branch Names")
	@BranchChoice(Scope.GLOBAL)
	@OmitName
	@NotNull
	@Size(min=1, message="At least one branch has to be selected.")
	public List<String> getDepotAndBranches() {
		return depotAndBranches;
	}

	public void setDepotAndBranches(List<String> depotAndBranches) {
		this.depotAndBranches = depotAndBranches;
	}

	@Override
	public boolean matches(Depot depot, String branch) {
		for (String each: getDepotAndBranches()) {
			DepotAndBranch depotAndBranch = new DepotAndBranch(each);
			if (depotAndBranch.getDepotId().equals(depot.getId()) && depotAndBranch.getBranch().equals(branch))
				return true;
		}
		return false;
	}

	@Override
	public Object trim(Object context) {
		DepotAndBranch.trim(depotAndBranches);
		if (!depotAndBranches.isEmpty())
			return this;
		else
			return null;
	}

}
