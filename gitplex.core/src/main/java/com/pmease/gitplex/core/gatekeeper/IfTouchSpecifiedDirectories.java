package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.editable.DirectoryChoice;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=90, icon="icon-folder-submodule", description=
		"This gate keeper will be passed if any commit files are under specified directories.")
public class IfTouchSpecifiedDirectories extends FileGateKeeper {

	private List<String> directories = new ArrayList<>();
	
	@Editable(name="Specify Directories", description="Use comma to separate multiple directories.")
	@DirectoryChoice
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
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);

			for (String file: update.getChangedFiles()) {
				for (String each: directories) {
					if (WildcardUtils.matchPath(each + "/**", file)) {
						request.setBaseUpdate(update);
						return approved("Touched directory '" + each + "'.");
					}
				}
			}
		}

		return disapproved("Not touched directories '" + getDirectories() + "'.");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		for (String each: directories) {
			if (file == null || WildcardUtils.matchPath(each + "/**", file)) 
				return approved("Touched directory '" + each + "'.");
		}
		return disapproved("Not touched directories '" + getDirectories() + "'.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		for (String file: branch.getRepository().git().listChangedFiles(branch.getHeadCommit(), commit)) {
			for (String each: directories) {
				if (WildcardUtils.matchPath(each + "/**", file))
					return approved("Touched directory '" + each + "'.");
			}
		}

		return disapproved("Not touched directories '" + getDirectories() + "'.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
