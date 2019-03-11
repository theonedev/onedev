package io.onedev.server.ci.jobtrigger;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.commons.utils.PathUtils;
import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.TagPatterns;

@Editable(order=200, name="When tags are created")
public class TagCreatedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String tag;
	
	@Editable(order=100, description="Optionally specify tag to match. Wildcard character * and ? may be used")
	@TagPatterns
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	protected boolean matches(ProjectEvent event, CISpec ciSpec, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String pushedTag = GitUtils.ref2tag(refUpdated.getRefName());
			if (pushedTag != null && !refUpdated.getNewCommitId().equals(ObjectId.zeroId()) 
					&& (getTag() == null || PathUtils.matchChildAware(getTag(), pushedTag))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		String condition;
		if (getTag() != null)
			condition = String.format("when tag %s is created", getTag());
		else
			condition = "when tags are created";
		
		if (isIgnore())
			return "Do not trigger " + condition;
		else
			return "Trigger " + condition;
	}

}
