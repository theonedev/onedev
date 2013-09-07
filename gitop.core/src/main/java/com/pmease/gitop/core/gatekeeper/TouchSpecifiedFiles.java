package com.pmease.gitop.core.gatekeeper;

import java.io.File;
import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.MergeRequestUpdate;

public class TouchSpecifiedFiles extends AbstractGateKeeper {

	private String filePaths;
	
	@NotEmpty
	public String getFilePaths() {
		return filePaths;
	}

	public void setFilePaths(String filePaths) {
		this.filePaths = filePaths;
	}

	@Override
	public CheckResult check(MergeRequest request) {
		RepositoryManager repositoryManager = AppLoader.getInstance(RepositoryManager.class);
		File repoDir = repositoryManager.locateStorage(request.getDestination().getRepository());

		FindChangedFilesCommand command = new Git(repoDir).findChangedFiles();
		
		for (int i=request.getEffectiveUpdates().size()-1; i>=0; i--) {
			MergeRequestUpdate update = request.getEffectiveUpdates().get(i);
			command.toRev(update.getRefName());
			if (i == 0) {
				command.fromRev(request.getMergeBase());
			} else {
				command.fromRev(request.getEffectiveUpdates().get(i-1).getRefName());
			}
			
			Collection<String> touchedFiles = command.call();
			for (String file: touchedFiles) {
				if (WildcardUtils.matchPath(getFilePaths(), file)) {
					request.setBaseUpdate(update);
					return accept("Touched files match pattern '" + getFilePaths() + "'.");
				}
			}
		}

		return reject("No touched files match pattern '" + getFilePaths() + "'.");
	}

}
