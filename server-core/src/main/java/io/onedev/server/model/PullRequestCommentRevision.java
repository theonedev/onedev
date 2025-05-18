package io.onedev.server.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.onedev.server.model.support.CommentRevision;

@Entity
public class PullRequestCommentRevision extends CommentRevision {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private PullRequestComment comment;

    public PullRequestComment getComment() {
        return comment;
    }

    public void setComment(PullRequestComment comment) {
        this.comment = comment;
    }
    
}
