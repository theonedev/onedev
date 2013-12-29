package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.Commit;
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
	public CheckResult doCheck(PullRequest request) {
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);

			Collection<String> touchedFiles;
			if (!update.getHeadCommit().startsWith(Commit.ZERO_HASH)) {
				touchedFiles = request.getTarget().getProject().code().listChangedFiles(
						update.getBaseCommit(), update.getHeadCommit());
			} else {
				touchedFiles = new ArrayList<>();
				String path = update.getHeadCommit().substring(Commit.ZERO_HASH.length());
				if (path.length() != 0) // test if a certain file can be touched
					touchedFiles.add(path);
				else // test if the branch can be deleted
					return accepted("Touched files match pattern '" + getFilePatterns() + "'.");
			} 
			
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
