package io.onedev.server.ci.jobtrigger;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.TagPattern;
import io.onedev.utils.PathUtils;

@Editable(order=200, name="When tags are created")
public class TagCreatedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tags;
	
	@Editable(order=100)
	@TagPattern
	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	protected boolean matches(ProjectEvent event, CISpec ciSpec, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String tag = GitUtils.ref2tag(refUpdated.getRefName());
			if (tag != null && !refUpdated.getNewCommitId().equals(ObjectId.zeroId()) 
					&& PathUtils.matchChildAware(getTags(), tag)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (getTags() != null)
			return "When tags '" + getTags() + "' are created ";
		else
			return "When tags are created";
	}

}
