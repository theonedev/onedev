package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import io.onedev.server.util.CommentAware;

public class PullRequestReopenData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private String comment;
	
	public PullRequestReopenData(@Nullable String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getActivity() {
		return "reopened";
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
				PullRequestReopenData.this.comment = comment;
			}
			
		};
	}

}
