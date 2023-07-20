package io.onedev.server.buildspec.job.trigger;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.job.TriggerMatch;
import io.onedev.server.event.project.ProjectEvent;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.ObjectId;

import java.util.List;

@Editable(order=200, name="Tag creation")
public class TagCreateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	private String branches;
	
	@Editable(name="Tags", order=100, placeholder="Any tag", description=""
			+ "Optionally specify space-separated tags to check. Use '**', '*' or '?' for "
			+ "<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all tags")
	@Patterns(suggester="suggestTags", path=true)
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

	@Editable(name="On Branches", order=200, placeholder="Any branch", description=""
			+ "This trigger will only be applicable if tagged commit is reachable from branches specified here. "
			+ "Multiple branches should be separated with spaces. Use '**', '*' or '?' for "
			+ "<a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty to match all branches")
	@Patterns(suggester="suggestBranches", path=true)
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
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String updatedTag = GitUtils.ref2tag(refUpdated.getRefName());
			ObjectId commitId = refUpdated.getNewCommitId();
			Project project = event.getProject();
			if (updatedTag != null && !commitId.equals(ObjectId.zeroId()) 
					&& (tags == null || PatternSet.parse(tags).matches(new PathMatcher(), updatedTag))
					&& (branches == null || project.isCommitOnBranches(commitId, PatternSet.parse(branches)))) {
				return new TriggerMatch(refUpdated.getRefName(), null, null, getParams(),
						"Tag '" + updatedTag + "' is created");
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		String description = "When create tags";
		if (tags != null)
			description += " '" + tags + "'";
		if (branches != null)
			description += " on branches '" + branches + "'";
		return description;
	}

}
