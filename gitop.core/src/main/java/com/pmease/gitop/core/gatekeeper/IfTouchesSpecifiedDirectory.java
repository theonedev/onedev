package com.pmease.gitop.core.gatekeeper;

import java.util.Collection;
import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.gatekeeper.FileGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=90, icon="icon-folder-submodule", description=
		"This gate keeper will be passed if any commit files are under specified directories.")
public class IfTouchesSpecifiedDirectory extends FileGateKeeper {

	private String directories;
	
	@Editable
	@NotEmpty
	public String getDirectories() {
		return directories;
	}

	public void setDirectories(String directories) {
		this.directories = directories;
	}

	@Override
	public CheckResult doCheck(PullRequest request) {
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);
			Collection<String> touchedFiles = request.getTarget().getProject().code().listChangedFiles(
					update.getBaseCommit(), update.getHeadCommit());
			
			List<String> dirList = StringUtils.splitAndTrim(getDirectories());
			for (String file: touchedFiles) {
				for (String each: dirList) {
					if (WildcardUtils.matchPath(each + "/**", file)) {
						request.setBaseUpdate(update);
						return accepted("Touched directory '" + each + "'.");
					}
				}
			}
		}

		return rejected("Not touched directories '" + getDirectories() + "'.");
	}

}
