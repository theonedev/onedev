package com.pmease.gitplex.core.branchmatcher;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.commons.wicket.editable.annotation.OmitName;
import com.pmease.gitplex.core.editable.BranchChoice;
import com.pmease.gitplex.core.editable.BranchChoice.Scope;
import com.pmease.gitplex.core.model.RepoAndBranch;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
@Editable(name="Specify Branch Names", order=100)
public class MatchAffinalBranchesByNames implements AffinalBranchMatcher {

	private List<String> repoAndBranches = new ArrayList<>();
	
	@Editable(name="Branch Names")
	@BranchChoice(Scope.AFFINAL)
	@OmitName
	@NotNull
	@Size(min=1, message="At least one branch has to be selected.")
	public List<String> getRepoAndBranches() {
		return repoAndBranches;
	}

	public void setRepoAndBranches(List<String> repoAndBranches) {
		this.repoAndBranches = repoAndBranches;
	}

	@Override
	public boolean matches(Repository repository, String branch) {
		for (String each: getRepoAndBranches()) {
			RepoAndBranch repoAndBranch = new RepoAndBranch(each);
			if (repoAndBranch.getRepoId().equals(repository.getId()) && repoAndBranch.getBranch().equals(branch))
				return true;
		}
		return false;
	}

	@Override
	public Object trim(Object context) {
		RepoAndBranch.trim(repoAndBranches);
		if (!repoAndBranches.isEmpty())
			return this;
		else
			return null;
	}

}
