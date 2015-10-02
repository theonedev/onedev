package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.editable.PathChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=90, icon="fa-folder-open", category=GateKeeper.CATEGROY_CHECK_FILES, description=
		"This gate keeper will be passed if any commit files are under specified directories.")
public class IfTouchSpecifiedDirectories extends AbstractGateKeeper {

	private List<String> directories = new ArrayList<>();
	
	@Editable(name="Specify Directories", description="Use comma to separate multiple directories.")
	@PathChoice
	@NotNull
	@Size(min=1, message="At least one directory has to be specified.")
	public List<String> getDirectories() {
		return directories;
	}

	public void setDirectories(List<String> directories) {
		this.directories = directories;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (PullRequestUpdate update: request.getEffectiveUpdates()) {
			for (String file: update.getChangedFiles()) {
				for (String each: directories) {
					if (WildcardUtils.matchPath(each + "/**", file)) {
						request.setReferentialUpdate(update);
						return passed(Lists.newArrayList("Touched directory '" + each + "'."));
					}
				}
			}
		}

		return failed(Lists.newArrayList("Not touched directories '" + getDirectories() + "'."));
	}

	@Override
	protected CheckResult doCheckFile(User user, Repository repository, String branch, String file) {
		if (file == null)
			return passed(new ArrayList<String>());
		for (String each: directories) {
			if (WildcardUtils.matchPath(each + "/**", file)) 
				return passed(Lists.newArrayList("Touched directory '" + each + "'."));
		}
		return failed(Lists.newArrayList("Not touched directories '" + getDirectories() + "'."));
	}

	@Override
	protected CheckResult doCheckCommit(User user, Repository repository, String branch, String commit) {
		for (String file: repository.git().listChangedFiles(repository.getObjectId(GitUtils.branch2ref(branch)).name(), commit, null)) {
			for (String each: directories) {
				if (WildcardUtils.matchPath(each + "/**", file))
					return passed(Lists.newArrayList("Touched directory '" + each + "'."));
			}
		}

		return failed(Lists.newArrayList("Not touched directories '" + getDirectories() + "'."));
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
