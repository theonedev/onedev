package io.onedev.server.event.pullrequest;

import java.util.Date;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.CommitAware;
import io.onedev.server.model.PullRequest;

public class PullRequestMergePreviewCalculated extends PullRequestEvent implements CommitAware {
	
	public PullRequestMergePreviewCalculated(PullRequest request) {
		super(null, new Date(), request);
	}

	@Override
	public ObjectId getCommitId() {
		return ObjectId.fromString(getRequest().getMergePreview().getMerged());
	}

}
