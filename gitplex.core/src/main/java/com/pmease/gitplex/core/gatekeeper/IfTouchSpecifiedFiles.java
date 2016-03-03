package com.pmease.gitplex.core.gatekeeper;

import org.eclipse.jgit.lib.ObjectId;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.annotation.PathMatch;
import com.pmease.gitplex.core.entity.Depot;
import com.pmease.gitplex.core.entity.PullRequest;
import com.pmease.gitplex.core.entity.PullRequestUpdate;
import com.pmease.gitplex.core.entity.Account;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.util.includeexclude.IncludeExcludeUtils;

@Editable(order=300, icon="fa-file-text", description=
		"This gate keeper will be passed if specified files are modified")
public class IfTouchSpecifiedFiles extends AbstractGateKeeper {

	private static final long serialVersionUID = 1L;
	
	private String pathMatch;
	
	@Editable(name="Files to Match")
	@PathMatch
	@NotEmpty
	public String getPathMatch() {
		return pathMatch;
	}

	public void setPathMatch(String pathMatch) {
		this.pathMatch = pathMatch;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (PullRequestUpdate update: request.getEffectiveUpdates()) {
			for (String file: update.getChangedFiles()) {
				if (IncludeExcludeUtils.matches(pathMatch, file)) {
					request.setReferentialUpdate(update);
					return passed(Lists.newArrayList("Touched files matches '" + pathMatch + "'"));
				}
			}
		}

		return failed(Lists.newArrayList("No touched files match '" + pathMatch + "'"));
	}

	@Override
	protected CheckResult doCheckFile(Account user, Depot depot, String branch, String file) {
		if (IncludeExcludeUtils.matches(pathMatch, file)) 
			return passed(Lists.newArrayList("Touched files match '" + pathMatch + "'"));
		else
			return failed(Lists.newArrayList("No touched files match '" + pathMatch + "'"));
	}

	@Override
	protected CheckResult doCheckPush(Account user, Depot depot, String refName, ObjectId oldCommit, ObjectId newCommit) {
		if (!oldCommit.equals(ObjectId.zeroId()) && !newCommit.equals(ObjectId.zeroId())) {
			for (String file: depot.git().listChangedFiles(oldCommit.name(), newCommit.name(), null)) {
				if (IncludeExcludeUtils.matches(pathMatch, file))
					return passed(Lists.newArrayList("Touched files match '" + pathMatch + "'"));
			}
			
			return failed(Lists.newArrayList("No touched files match '" + pathMatch + "'"));
		} else {
			return ignored();
		}
	}

}
