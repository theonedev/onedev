package io.onedev.server.model;

import java.io.Serial;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import io.onedev.server.model.support.CommentRevision;

@Entity
public class PullRequestCommentRevision extends CommentRevision {

	@Serial
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
