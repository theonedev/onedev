package io.onedev.server.buildspec.job.trigger;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

import java.util.Collection;
import java.util.List;

@Editable(order=100, name="Branch update", description=""
		+ "Job will run when code is committed. <b class='text-info'>NOTE:</b> This trigger will ignore commits "
		+ "with message containing <code>[skip ci]</code>, <code>[ci skip]</code>, <code>[no ci]</code>, "
		+ "<code>[skip job]</code>, <code>[job skip]</code>, or <code>[no job]</code>")

public class BranchUpdateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;
	
	private String paths;
	
	@Editable(name="Branches", order=100, placeholder="Any branch", description="Optionally specify space-separated branches "
			+ "to check. Use '**' or '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester = "suggestBranches", path=true)
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestBranches(String matchWith) {
		return SuggestionUtils.suggestBranches(Project.get(), matchWith);
	}
	
	@Editable(name="Touched Files", order=200, placeholder="Any file", 
			description="Optionally specify space-separated files to check. Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
					+ "Prefix with '-' to exclude. Leave empty to match all files")
	@Patterns(suggester = "getPathSuggestions", path=true)
	public String getPaths() {
		return paths;
	}

	public void setPaths(String paths) {
		this.paths = paths;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> getPathSuggestions(String matchWith) {
		return SuggestionUtils.suggestBlobs(Project.get(), matchWith);
	}

	private boolean touchedFile(RefUpdated refUpdated) {
		if (getPaths() != null) {
			if (refUpdated.getOldCommitId().equals(ObjectId.zeroId())) {
				return true;
			} else if (refUpdated.getNewCommitId().equals(ObjectId.zeroId())) {
				return false;
			} else {
				Repository repository = OneDev.getInstance(ProjectManager.class)
						.getRepository(refUpdated.getProject().getId());
				Collection<String> changedFiles = GitUtils.getChangedFiles(
						repository, 
						refUpdated.getOldCommitId(), refUpdated.getNewCommitId());
				PatternSet patternSet = PatternSet.parse(getPaths());
				Matcher matcher = new PathMatcher();
				for (String changedFile: changedFiles) {
					if (patternSet.matches(matcher, changedFile))
						return true;
				}
				return false;
			}
		} else {
			return true;
		}
	}
	
	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String updatedBranch = GitUtils.ref2branch(refUpdated.getRefName());
			Matcher matcher = new PathMatcher();
			if (updatedBranch != null
					&& !SKIP_COMMIT.apply(event.getProject().getRevCommit(refUpdated.getNewCommitId(), true))
					&& (branches == null || PatternSet.parse(branches).matches(matcher, updatedBranch)) 
					&& touchedFile(refUpdated)) {
				return new TriggerMatch(refUpdated.getRefName(), null, null,
						getParams(), "Branch '" + updatedBranch + "' is updated");
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		String description;
		if (getBranches() != null && getPaths() != null)
			description = String.format("When update branches '%s' and touch files '%s'", getBranches(), getPaths());
		else if (getBranches() != null)
			description = String.format("When update branches '%s'", getBranches());
		else if (getPaths() != null)
			description = String.format("When touch files '%s'", getPaths());
		else
			description = "When update branches";
		return description;
	}

}
