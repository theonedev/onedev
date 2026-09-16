package io.onedev.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import io.onedev.server.model.support.CommentRevision;

@Entity
public class IssueCommentRevision extends CommentRevision {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(nullable=false)
	private IssueComment comment;

    public IssueComment getComment() {
        return comment;
    }

    public void setComment(IssueComment comment) {
        this.comment = comment;
    }
    
}
