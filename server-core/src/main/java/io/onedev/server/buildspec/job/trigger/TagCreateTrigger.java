package io.onedev.server.buildspec.job.trigger;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.SubmitReason;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="Tag creation")
public class TagCreateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	private String branches;
	
	@Editable(name="Tags", order=100, description="Optionally specify space-separated tags to check. "
			+ "Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all tags")
	@Patterns(suggester="suggestTags", path=true)
	@NameOfEmptyValue("Any tag")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestTags(String matchWith) {
		return SuggestionUtils.suggestTags(Project.get(), matchWith);
	}

	@Editable(name="On Branches", order=200, description="This trigger will only be applicable "
			+ "if tagged commit is on branches specified here. Multiple branches should be "
			+ "separated with spaces. Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester="suggestBranches", path=true)
	@NameOfEmptyValue("Any branch")
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
	
	@Override
	public SubmitReason matchesWithoutProject(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String updatedTag = GitUtils.ref2tag(refUpdated.getRefName());
			ObjectId commitId = refUpdated.getNewCommitId();
			Project project = event.getProject();
			if (updatedTag != null && !commitId.equals(ObjectId.zeroId()) 
					&& (tags == null || PatternSet.parse(tags).matches(new PathMatcher(), updatedTag))
					&& (branches == null || project.isCommitOnBranches(commitId, branches))) {
				return new SubmitReason() {

					@Override
					public String getRefName() {
						return refUpdated.getRefName();
					}

					@Override
					public PullRequest getPullRequest() {
						return null;
					}

					@Override
					public String getDescription() {
						return "Tag '" + updatedTag + "' is created";
					}
					
				};
			}
		}
		return null;
	}

	@Override
	public String getDescriptionWithoutProject() {
		String description = "When create tags";
		if (tags != null)
			description += " '" + tags + "'";
		if (branches != null)
			description += " on branches '" + branches + "'";
		return description;
	}

}
