package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import org.apache.wicket.Component;

import io.onedev.server.OneDev;
import io.onedev.server.entitymanager.PullRequestChangeManager;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestChange;
import io.onedev.server.util.CommentAware;

public class PullRequestRequestedForChangesData implements PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private String comment;
	
	public PullRequestRequestedForChangesData(@Nullable String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getActivity(PullRequest withRequest) {
		String activity = "requested for changes";
		if (withRequest != null)
			activity += " in pull request " + withRequest.getNumberAndTitle();
		return activity;
	}

	@Override
	public CommentAware getCommentAware() {
		return new CommentAware() {

			private static final long serialVersionUID = 1L;

			@Override
			public String getComment() {
				return comment;
			}

			@Override
			public void setComment(String comment) {
				PullRequestRequestedForChangesData.this.comment = comment;
			}
			
		};
	}

	@Override
	public Component render(String componentId, PullRequestChange change) {
		Long changeId = change.getId();
		return new PullRequestChangeCommentPanel(componentId) {

			private static final long serialVersionUID = 1L;

			@Override
			protected PullRequestChange getChange() {
				return OneDev.getInstance(PullRequestChangeManager.class).load(changeId);
			}
			
		};		
	}

}
