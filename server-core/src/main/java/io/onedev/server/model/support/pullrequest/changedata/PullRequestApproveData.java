package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentSupport;
import io.onedev.server.util.diff.DiffSupport;
import io.onedev.server.web.page.project.pullrequests.detail.activities.activity.DiffAndCommentAwarePanel;

public class PullRequestApproveData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private String comment;
	
	public PullRequestApproveData(@Nullable String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "approved";
		if (withRequest != null)
			activity += " pull request " + withRequest.describe();
		return activity;
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		Long changeId = change.getId();
		return new DiffAndCommentAwarePanel(componentId) {
			
			private static final long serialVersionUID = 1L;

			@Override
			protected PullRequestChange getChange() {
				return OneDev.getInstance(PullRequestChangeManager.class).load(changeId);
			}

			@Override
			protected DiffSupport getDiffSupport() {
				return null;
			}
		};
	}
	
	@Override
	public CommentSupport getCommentSupport() {
		return new CommentSupport() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getComment() {
				return comment;
			}

			@Override
			public void setComment(String comment) {
				PullRequestApproveData.this.comment = comment;
			}
			
		};
	}

}
