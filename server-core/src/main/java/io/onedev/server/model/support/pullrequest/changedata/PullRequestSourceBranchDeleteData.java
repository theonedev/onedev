package io.onedev.server.model.support.pullrequest.changedata;

import javax.annotation.Nullable;

import io.onedev.server.util.CommentAware;

public class PullRequestSourceBranchDeleteData extends PullRequestChangeData {

	private static final long serialVersionUID = 1L;

	private String comment;
	
	public PullRequestSourceBranchDeleteData(@Nullable String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getActivity() {
		return "deleted source branch";
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
				PullRequestSourceBranchDeleteData.this.comment = comment;
			}
			
		};
	}

}
