package io.onedev.server.ci.jobtrigger;

import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.PathUtils;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.editable.annotation.BranchPatterns;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.PathPatterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=100, name="When pushes to branches")
public class BranchPushedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String branch;
	
	private String path;
	
	@Editable(order=100, 
			description="Optionally specify branch to match. Wildcard character * and ? may be used")
	@BranchPatterns
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Editable(name="File", order=200, 
			description="Optionally specify file to match. Wildcard character * and ? may be used")
	@PathPatterns("getPathSuggestions")
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> getPathSuggestions(String matchWith) {
		return SuggestionUtils.suggestBlobs(OneContext.get().getProject(), matchWith);
	}

	private boolean touchedFile(RefUpdated refUpdated) {
		if (getPath() != null) {
			if (refUpdated.getOldCommitId().equals(ObjectId.zeroId())) {
				return true;
			} else if (refUpdated.getNewCommitId().equals(ObjectId.zeroId())) {
				return false;
			} else {
				Collection<String> changedFiles = GitUtils.getChangedFiles(refUpdated.getProject().getRepository(), 
						refUpdated.getOldCommitId(), refUpdated.getNewCommitId());
				for (String changedFile: changedFiles) {
					if (PathUtils.matchChildAware(getPath(), changedFile))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}
	
	@Override
	protected boolean matches(ProjectEvent event, CISpec ciSpec, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String pushedBranch = GitUtils.ref2branch(refUpdated.getRefName());
			if (pushedBranch != null) {
				if ((getBranch() == null || PathUtils.matchChildAware(getBranch(), pushedBranch)) 
						&& touchedFile(refUpdated)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		String condition;
		if (getBranch() != null && getPath() != null)
			condition = String.format("when push to branch %s and touch file %s", getBranch(), getPath());
		else if (getBranch() != null)
			condition = String.format("when push to branch %s", getBranch());
		else if (getPath() != null)
			condition = String.format("when push to branch %s", getBranch());
		else
			condition = "when push to branches";
		if (isIgnore())
			return "Do not trigger " + condition;
		else
			return "Trigger " + condition;
	}

}
