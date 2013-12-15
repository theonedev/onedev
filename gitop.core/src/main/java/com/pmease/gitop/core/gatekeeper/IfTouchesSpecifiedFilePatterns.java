package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.gatekeeper.FileGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=100, icon="icon-file-text", description=
		"This gate keeper will be passed if any commit file maches specified file patterns.")
public class IfTouchesSpecifiedFilePatterns extends FileGateKeeper {

	private String filePatterns;
	
	@Editable
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}

	@Override
	public CheckResult doCheck(PullRequest request) {
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);
			Collection<String> touchedFiles = request.getTarget().getProject().code().listChangedFiles(
					update.getBaseCommit(), update.getHeadCommit());
			
			for (String file: touchedFiles) {
				if (WildcardUtils.matchPath(getFilePatterns(), file)) {
					request.setBaseUpdate(update);
					return accepted("Touched files match pattern '" + getFilePatterns() + "'.");
				}
			}
		}

		return rejected("No touched files match pattern '" + getFilePatterns() + "'.");
	}

}
