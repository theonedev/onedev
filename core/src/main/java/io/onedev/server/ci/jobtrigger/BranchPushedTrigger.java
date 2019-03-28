package io.onedev.server.ci.jobtrigger;

import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.PathUtils;
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

	private String branches;
	
	private String paths;
	
	@Editable(name="Pushed Branches", order=100, 
			description="Optionally specify space-separated branches to check. Use * or ? for wildcard match")
	@BranchPatterns
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(name="Touched Files", order=200, 
			description="Optionally specify space-separated files to check. Use * or ? for wildcard match")
	@PathPatterns("getPathSuggestions")
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> getPathSuggestions(String matchWith) {
		return SuggestionUtils.suggestBlobs(OneContext.get().getProject(), matchWith);
	}

	private boolean touchedFile(RefUpdated refUpdated) {
		if (getPaths() != null) {
			if (refUpdated.getOldCommitId().equals(ObjectId.zeroId())) {
				return true;
			} else if (refUpdated.getNewCommitId().equals(ObjectId.zeroId())) {
				return false;
			} else {
				Collection<String> changedFiles = GitUtils.getChangedFiles(refUpdated.getProject().getRepository(), 
						refUpdated.getOldCommitId(), refUpdated.getNewCommitId());
				for (String changedFile: changedFiles) {
					if (PathUtils.matchChildAware(getPaths(), changedFile))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}
	
	@Override
	public boolean matches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String pushedBranch = GitUtils.ref2branch(refUpdated.getRefName());
			if (pushedBranch != null) {
				if ((getBranches() == null || PathUtils.matchChildAware(getBranches(), pushedBranch)) 
						&& touchedFile(refUpdated)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (getBranches() != null && getPaths() != null)
			return String.format("When push to branches '%s' and touch files '%s'", getBranches(), getPaths());
		else if (getBranches() != null)
			return String.format("When push to branches '%s'", getBranches());
		else if (getPaths() != null)
			return String.format("When touch files '%s'", getBranches());
		else
			return "When push to branches";
	}

}
