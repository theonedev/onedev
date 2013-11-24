package com.pmease.gitop.core.gatekeeper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Git;
import com.pmease.commons.git.TreeNode;
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
					touchedFiles = new ArrayList<>();
					for (TreeNode each: new Git(repoDir).listTree(update.getRefName(), null, true)) {
						if (each.getType() == TreeNode.Type.FILE)
							touchedFiles.add(each.getPath());
					}
				} else {
					touchedFiles = new Git(repoDir).listChangedFiles(request.getMergeBase(), update.getRefName());
				}
			} else {
				touchedFiles = new Git(repoDir).listChangedFiles(
						request.getEffectiveUpdates().get(i+1).getRefName(), 
						update.getRefName());
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
