package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.DiffAndCommentAwarePanel;

public class PullRequestMergeData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reason;
	
	public PullRequestMergeData(@Nullable String reason) {
		this.reason = reason;
	}
	
	@Override
	public String getDescription() {
		if (reason != null)
			return reason;
		else
			return "Pull request was merged";
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		return new DiffAndCommentAwarePanel(componentId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected PullRequestChange getChange() {
				return change;
			}

			@Override
			protected DiffSupport getDiffSupport() {
				return null;
			}
		};
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
	}


}
