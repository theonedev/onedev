package io.onedev.server.buildspec.job.trigger;

import java.util.List;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.Patterns;
import io.onedev.server.web.util.SuggestionUtils;

@Editable(order=200, name="When create tags")
public class TagCreateTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	@Editable(name="Tags", order=100, 
			description="Optionally specify space-separated tags to check. Use * or ? for wildcard match. "
					+ "Leave empty to match all tags")
	@Patterns(suggester = "suggestTags")
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

	@Override
	public boolean matches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String pushedTag = GitUtils.ref2tag(refUpdated.getRefName());
			if (pushedTag != null && !refUpdated.getNewCommitId().equals(ObjectId.zeroId()) 
					&& (getTags() == null || PatternSet.parse(getTags()).matches(new PathMatcher(), pushedTag))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (getTags() != null)
			return String.format("When create tags '%s'", getTags());
		else
			return "When create tags";
	}

}
