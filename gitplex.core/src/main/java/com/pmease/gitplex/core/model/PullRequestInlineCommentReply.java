package com.pmease.gitplex.core.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("serial")
@Entity
public class PullRequestInlineCommentReply extends AbstractPullRequestCommentReply {

	@ManyToOne
	@JoinColumn(nullable=false)
	private PullRequestInlineComment comment;
	
	@Override
	public PullRequestInlineComment getComment() {
		return comment;
	}

	public void setComment(PullRequestInlineComment comment) {
		this.comment = comment;
	}

}
