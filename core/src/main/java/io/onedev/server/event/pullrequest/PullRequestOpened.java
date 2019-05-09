package io.onedev.server.event.pullrequest;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.support.pullrequest.MergePreview;

public class PullRequestOpened extends PullRequestEvent implements MarkdownAware {

	public PullRequestOpened(PullRequest request) {
		super(request.getSubmitter(), request.getSubmitDate(), request);
	}

	@Override
	public String getMarkdown() {
		return getRequest().getDescription();
	}

	@Override
	public Collection<ObjectId> getBuildCommits() {
		Collection<ObjectId> commitIds = super.getBuildCommits();
		MergePreview preview = getRequest().getMergePreview();
		if (preview != null && preview.getMerged() != null)
			commitIds.add(ObjectId.fromString(preview.getMerged()));
		return commitIds;
	}

}
