package com.pmease.gitplex.core.gatekeeper;

import java.util.ArrayList;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.collect.Lists;
import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.commons.wicket.editable.annotation.Editable;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Depot;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-file-text", category=GateKeeper.CATEGROY_CHECK_FILES, description=
		"This gate keeper will be passed if any commit file maches specified file patterns.")
public class IfTouchSpecifiedFilePatterns extends AbstractGateKeeper {

	private String filePatterns;
	
	@Editable(name="Specify File Patterns", description="Specify <a href='http://wiki.pmease.com/display/gp/Pattern+Set'>file patterns</a> to match.")
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (PullRequestUpdate update: request.getEffectiveUpdates()) {
			for (String file: update.getChangedFiles()) {
				if (WildcardUtils.matchPath(getFilePatterns(), file)) {
					request.setReferentialUpdate(update);
					return passed(Lists.newArrayList("Touched files match patterns '" + getFilePatterns() + "'."));
				}
			}
		}

		return failed(Lists.newArrayList("No touched files match patterns '" + getFilePatterns() + "'."));
	}

	@Override
	protected CheckResult doCheckFile(User user, Depot depot, String branch, String file) {
		if (file == null)
			return passed(new ArrayList<String>());
		else if (WildcardUtils.matchPath(filePatterns, file)) 
			return passed(Lists.newArrayList("Touched files match patterns '" + filePatterns + "'."));
		else
			return failed(Lists.newArrayList("No touched files match patterns '" + filePatterns + "'."));
	}

	@Override
	protected CheckResult doCheckCommit(User user, Depot depot, String branch, String commit) {
		for (String file: depot.git().listChangedFiles(depot.getObjectId(GitUtils.branch2ref(branch)).name(), commit, null)) {
			if (WildcardUtils.matchPath(filePatterns, file))
					return passed(Lists.newArrayList("Touched files match patterns '" + filePatterns + "'."));
		}
		
		return failed(Lists.newArrayList("No touched files match patterns '" + filePatterns + "'."));
	}

	@Override
	protected CheckResult doCheckRef(User user, Depot depot, String refName) {
		return ignored();
	}

}
