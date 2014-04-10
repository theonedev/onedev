package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.Repository;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.FileGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=100, icon="icon-file-text", description=
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
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);

			Collection<String> touchedFiles = request.git()
					.listChangedFiles(update.getBaseCommit(), update.getHeadCommit());
			
			for (String file: touchedFiles) {
				if (WildcardUtils.matchPath(getFilePatterns(), file)) {
					request.setBaseUpdate(update);
					return approved("Touched files match patterns '" + getFilePatterns() + "'.");
				}
			}
		}

		return disapproved("No touched files match patterns '" + getFilePatterns() + "'.");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, String file) {
		if (WildcardUtils.matchPath(filePatterns, file)) 
			return approved("Touched files match patterns '" + filePatterns + "'.");
		else
			return disapproved("No touched files match patterns '" + filePatterns + "'.");
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		for (String file: branch.getProject().git().listChangedFiles(branch.getHeadCommit(), commit)) {
			if (WildcardUtils.matchPath(filePatterns, file))
					return approved("Touched files match patterns '" + filePatterns + "'.");
		}
		
		return disapproved("No touched files match patterns '" + filePatterns + "'.");
	}

	@Override
	protected CheckResult doCheckRef(User user, Repository project, String refName) {
		return ignored();
	}

}
