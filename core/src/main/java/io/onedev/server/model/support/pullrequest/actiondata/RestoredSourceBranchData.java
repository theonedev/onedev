package io.onedev.server.model.support.pullrequest.actiondata;

import javax.annotation.Nullable;

import io.onedev.server.model.support.CommentSupport;

public class RestoredSourceBranchData extends ActionData {

	private static final long serialVersionUID = 1L;

	private String comment;
	
	public RestoredSourceBranchData(@Nullable String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getDescription() {
		return "restored source branch";
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
				RestoredSourceBranchData.this.comment = comment;
			}
			
		};
	}

}
