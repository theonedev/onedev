package com.pmease.gitop.core.gatekeeper;

import java.io.File;
import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.ListChangedFilesCommand;
import com.pmease.commons.git.ListFilesCommand;
import com.pmease.commons.git.Git;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitop.core.manager.StorageManager;
import com.pmease.gitop.core.model.MergeRequest;
import com.pmease.gitop.core.model.MergeRequestUpdate;

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
	public CheckResult check(MergeRequest request) {
		StorageManager storageManager = AppLoader.getInstance(StorageManager.class);
		File repoDir = storageManager.getStorage(request.getTarget().getProject()).ofCode();

		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			MergeRequestUpdate update = request.getEffectiveUpdates().get(i);
			Collection<String> touchedFiles;
			if (i == request.getEffectiveUpdates().size()-1) {
				if (request.getMergeBase() == null) {
					ListFilesCommand command = new Git(repoDir).listFiles();
					command.revision(update.getRefName());
					touchedFiles = command.call();
				} else {
					ListChangedFilesCommand command = new Git(repoDir).listChangedFiles();
					command.fromRev(request.getMergeBase());
					command.toRev(update.getRefName());
					touchedFiles = command.call();
				}
			} else {
				ListChangedFilesCommand command = new Git(repoDir).listChangedFiles();
				command.fromRev(request.getEffectiveUpdates().get(i+1).getRefName());
				command.toRev(update.getRefName());
				touchedFiles = command.call();
			}
			
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
