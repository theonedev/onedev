package io.onedev.server.event.pullrequest;

import io.onedev.server.event.MarkdownAware;
import io.onedev.server.model.PullRequestChange;

public class PullRequestChangeEvent extends PullRequestEvent implements MarkdownAware {

	private final PullRequestChange change;
	
	public PullRequestChangeEvent(PullRequestChange change) {
		super(change.getUser(), change.getDate(), change.getRequest());
		this.change = change;
	}

	public PullRequestChange getChange() {
		return change;
	}

	@Override
	public String getMarkdown() {
		if (change.getData().getCommentAware() != null)
			return change.getData().getCommentAware().getComment();
		else
			return null;
	}

	@Override
	public String getActivity(boolean withEntity) {
		return change.getData().getActivity(withEntity?change.getRequest():null);
	}

}
