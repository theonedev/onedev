package com.pmease.gitop.core.model.gatekeeper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.git.Git;
import com.pmease.commons.git.FindChangedFilesCommand;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.core.manager.RepositoryManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.MergeRequestUpdate;

public class TouchedSpecifiedFiles extends AbstractGateKeeper {

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
		List<MergeRequestUpdate> updates = new ArrayList<MergeRequestUpdate>(request.getUpdates());
		Collections.sort(updates);
		
		RepositoryManager repositoryManager = AppLoader.getInstance(RepositoryManager.class);
		File repoDir = repositoryManager.locateStorage(request.getDestination().getRepository());

		for (int i=updates.size()-1; i>=0; i--) {
			MergeRequestUpdate update = updates.get(i);
			FindChangedFilesCommand command = new Git(repoDir).findChangedFiles();
			command.toRev(update.getRefName());
			if (i-1 < 0) {
				command.fromRev(request.getDestination().getName());
			} else {
				command.fromRev(updates.get(i-1).getRefName());
			}
			
			Collection<String> touchedFiles = command.call();
			for (String file: touchedFiles) {
				if (WildcardUtils.matchPath(getFilePaths(), file)) {
					request.setBaseUpdate(update);
					return CheckResult.ACCEPT;
				}
			}
			
		}

		return CheckResult.REJECT;
	}

}
