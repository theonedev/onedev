package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.gatekeeper.AbstractGateKeeper;
import com.pmease.gitop.model.gatekeeper.GateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(category=GateKeeper.CATEGORY_FILE, order=100, icon="icon-folder-submodule", description=
		"This condition will be satisified if commit files contains any of specified files.")
public class TouchSpecifiedFiles extends AbstractGateKeeper {

	private String filePaths;
	
	@Editable
	@NotEmpty
	public String getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(String filePaths) {
		this.filePaths = filePaths;
	}

	@Override
	public CheckResult check(PullRequest request) {
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);
			Collection<String> touchedFiles = request.getTarget().getProject().code().listChangedFiles(
					update.getBaseCommit(), update.getHeadCommit());
			
			for (String file: touchedFiles) {
				if (WildcardUtils.matchPath(getFilePaths(), file)) {
					request.setBaseUpdate(update);
					return accepted("Touched files match pattern '" + getFilePaths() + "'.");
				}
			}
		}

		return rejected("No touched files match pattern '" + getFilePaths() + "'.");
	}

}
