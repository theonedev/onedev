package io.onedev.server.model.support.pullrequest.changedata;

import org.apache.wicket.Component;

import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.DiffAndCommentAwarePanel;

public class PullRequestReviewerRemoveData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private final String reviewer;
	
	public PullRequestReviewerRemoveData(String reviewer) {
		this.reviewer = reviewer;
	}
	
	@Override
	public String getDescription() {
		return "removed reviewer \"" + reviewer + "\"";
	}

	@Override
	public CommentSupport getCommentSupport() {
		return null;
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

}
