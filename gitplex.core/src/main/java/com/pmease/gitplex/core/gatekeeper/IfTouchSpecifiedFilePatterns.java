package com.pmease.gitplex.core.gatekeeper;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.model.Branch;
import com.pmease.gitplex.core.model.PullRequest;
import com.pmease.gitplex.core.model.PullRequestUpdate;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;

@SuppressWarnings("serial")
@Editable(order=100, icon="fa-file-txt", description=
		"This gate keeper will be passed if any commit file maches specified file patterns.")
public class IfTouchSpecifiedFilePatterns extends FileGateKeeper {

	private String filePatterns;
	
	@Editable(name="Specify File Patterns", description="Specify file patterns to match. Below is some examples:"
			+ "<ul>"
			+ "<li><i>src/*</i>: matches all files directly under src."
			+ "<li><i>src/**</i>: matches all files under src recursively."
			+ "<li><i>**</i>: matches all files."
			+ "<li><i>**/*.c, **/*.java</i>: matches all C and Java files."
			+ "<li><i>-src/**, **</i>: matches all files except those under src."
			+ "</ul>")
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
					return approved("Touched files match patterns '" + getFilePatterns() + "'.");
				}
			}
		}

		return disapproved("No touched files match patterns '" + getFilePatterns() + "'.");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		if (file == null || WildcardUtils.matchPath(filePatterns, file)) 
			return approved("Touched files match patterns '" + filePatterns + "'.");
		else
			return disapproved("No touched files match patterns '" + filePatterns + "'.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		for (String file: branch.getRepository().git().listChangedFiles(branch.getHeadCommitHash(), commit, null)) {
			if (WildcardUtils.matchPath(filePatterns, file))
					return approved("Touched files match patterns '" + filePatterns + "'.");
		}
		
		return disapproved("No touched files match patterns '" + filePatterns + "'.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository repository, String refName) {
		return ignored();
	}

}
