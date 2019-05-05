package io.onedev.server.ci.job.trigger;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.ci.job.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import io.onedev.server.web.editable.annotation.TagPatterns;

@Editable(order=200, name="When tags are created")
public class TagCreatedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	@Editable(name="Created Tags", order=100, 
			description="Optionally specify space-separated tags to check. Use * or ? for wildcard match. "
					+ "Leave empty to match all tags")
	@TagPatterns
	@NameOfEmptyValue("Any tag")
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	public boolean matches(ProjectEvent event, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String pushedTag = GitUtils.ref2tag(refUpdated.getRefName());
			if (pushedTag != null && !refUpdated.getNewCommitId().equals(ObjectId.zeroId()) 
					&& (getTags() == null || PathUtils.matchChildAware(getTags(), pushedTag))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (getTags() != null)
			return String.format("When tags '%s' are created", getTags());
		else
			return "When tags are created";
	}

}
