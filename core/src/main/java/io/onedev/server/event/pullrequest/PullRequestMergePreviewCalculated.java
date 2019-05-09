package io.onedev.server.event.pullrequest;

import java.util.Collection;
import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.model.PullRequest;

public class PullRequestMergePreviewCalculated extends PullRequestEvent {
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public Collection<ObjectId> getBuildCommits() {
		Collection<ObjectId> commitIds = super.getBuildCommits();
		if (getRequest().getMergePreview().getMerged() != null)
			commitIds.add(ObjectId.fromString(getRequest().getMergePreview().getMerged()));
		return commitIds;
	}

}
