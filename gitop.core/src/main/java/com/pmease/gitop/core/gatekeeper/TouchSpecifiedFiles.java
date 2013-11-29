package com.pmease.gitop.core.gatekeeper;

import java.io.File;
import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.model.PullRequest;
import com.pmease.gitop.core.model.PullRequestUpdate;

@SuppressWarnings("serial")
@Editable
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
		StorageManager storageManager = AppLoader.getInstance(StorageManager.class);
		File repoDir = storageManager.getStorage(request.getTarget().getProject()).ofCode();

		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);
			Collection<String> touchedFiles = new Git(repoDir).listChangedFiles(
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
