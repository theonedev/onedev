package io.onedev.server.ci.jobtrigger;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.ci.CISpec;
import io.onedev.server.ci.Job;
import io.onedev.server.event.ProjectEvent;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.GitUtils;
import io.onedev.server.web.editable.annotation.BranchPattern;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.PathPattern;
import io.onedev.utils.PathUtils;

@Editable(order=100, name="When pushes to branches")
public class BranchPushedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	private String branches;
	
	private String path;
	
	@Editable(order=100)
	@BranchPattern
	public String getBranches() {
		return branches;
	}

	public void setBranches(String branches) {
		this.branches = branches;
	}

	@Editable(order=200)
	@PathPattern
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	protected boolean matches(ProjectEvent event, CISpec ciSpec, Job job) {
		if (event instanceof RefUpdated) {
			RefUpdated refUpdated = (RefUpdated) event;
			String branch = GitUtils.ref2branch(refUpdated.getRefName());
			if (branch != null && PathUtils.matchChildAware(getBranches(), branch)) {
				if (refUpdated.getOldCommitId().equals(ObjectId.zeroId())) {
					return true;
				} else if (!refUpdated.getNewCommitId().equals(ObjectId.zeroId())) {
					if (getPath() != null) {
						Collection<String> changedFiles = GitUtils.getChangedFiles(event.getProject().getRepository(), 
								refUpdated.getOldCommitId(), refUpdated.getNewCommitId());
						for (String changedFile: changedFiles) {
							if (PathUtils.matchChildAware(getPath(), changedFile))
								return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String getDescription() {
		if (getBranches() != null && getPath() != null)
			return "When push to branches '" + getBranches() + "' on path '" + getPath() + "'";
		else if (getBranches() != null)
			return "When push to branches '" + getBranches() + "'";
		else if (getPath() != null)
			return "When push to branches '" + getPath() + "'";
		else
			return "When push to branches";
	}

}
